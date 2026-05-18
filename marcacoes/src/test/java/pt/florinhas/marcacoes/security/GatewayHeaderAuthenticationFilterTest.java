package pt.florinhas.marcacoes.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;

import pt.florinhas.common_data.security.CustomUserDetailsService;

class GatewayHeaderAuthenticationFilterTest {

    private CustomUserDetailsService userDetailsService;
    private GatewayHeaderAuthenticationFilter filter;
    private static final String SHARED_SECRET = "super-secret-key";

    @BeforeEach
    void setUp() {
        userDetailsService = mock(CustomUserDetailsService.class);
        filter = new GatewayHeaderAuthenticationFilter(userDetailsService, SHARED_SECRET);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve ignorar o filtro se o contexto já estiver autenticado")
    void doFilterInternal_DeveFazerNadaSeJaAutenticado() throws ServletException, IOException {
        UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken(
                "existingUser", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertSame(existingAuth, SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("Deve ignorar o filtro se o cabeçalho do usuário estiver ausente")
    void doFilterInternal_DeveFazerNadaSeUsuarioNaoInformado() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("Deve rejeitar com 401 se o gateway secret estiver ausente")
    void doFilterInternal_DeveRejeitarSeSecretAusente() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Authenticated-User", "teste@teste.com");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized gateway origin", response.getErrorMessage());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Deve rejeitar com 401 se o gateway secret for incorreto")
    void doFilterInternal_DeveRejeitarSeSecretInvalido() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Authenticated-User", "teste@teste.com");
        request.addHeader("X-Gateway-Secret", "secret-errado");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized gateway origin", response.getErrorMessage());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Deve rejeitar com 401 se o gateway secret esperado no servidor for vazio")
    void doFilterInternal_DeveRejeitarSeSecretEsperadoForVazio() throws ServletException, IOException {
        GatewayHeaderAuthenticationFilter filterEmptySecret = new GatewayHeaderAuthenticationFilter(userDetailsService, "");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Authenticated-User", "teste@teste.com");
        request.addHeader("X-Gateway-Secret", SHARED_SECRET);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filterEmptySecret.doFilterInternal(request, response, chain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized gateway origin", response.getErrorMessage());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Deve autenticar com sucesso e mapear as roles do cabeçalho")
    void doFilterInternal_DeveAutenticarComRolesDoHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Authenticated-User", "teste@teste.com");
        request.addHeader("X-Gateway-Secret", SHARED_SECRET);
        request.addHeader("X-Authenticated-Roles", "ROLE_USER, ROLE_ADMIN");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        Utilizador user = new Utente();
        user.setEmail("teste@teste.com");
        when(userDetailsService.loadUserByUsername("teste@teste.com")).thenReturn(user);

        filter.doFilterInternal(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(user, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertEquals(2, authentication.getAuthorities().size());
    }

    @Test
    @DisplayName("Deve recorrer às authorities do usuário se o cabeçalho de roles for nulo ou vazio")
    void doFilterInternal_DeveUsarAuthoritiesDoUsuarioSeRolesDoHeaderEstivereVazio()
            throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Authenticated-User", "teste@teste.com");
        request.addHeader("X-Gateway-Secret", SHARED_SECRET);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        Utilizador user = mock(Utilizador.class);
        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_DEFAULT"));
        doReturn(authorities).when(user).getAuthorities();
        when(userDetailsService.loadUserByUsername("teste@teste.com")).thenReturn(user);

        filter.doFilterInternal(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DEFAULT")));
        assertEquals(1, authentication.getAuthorities().size());
    }

    @Test
    @DisplayName("Deve rejeitar com 401 se o userDetailsService lançar uma exceção ao carregar usuário")
    void doFilterInternal_DeveRejeitarSeUsuarioNaoExistirNoService() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Authenticated-User", "inexistente@teste.com");
        request.addHeader("X-Gateway-Secret", SHARED_SECRET);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(userDetailsService.loadUserByUsername("inexistente@teste.com"))
                .thenThrow(new UsernameNotFoundException("Usuário não encontrado"));

        filter.doFilterInternal(request, response, chain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Invalid authenticated user", response.getErrorMessage());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}