package pt.florinhas.marcacoes.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;

class GatewayHeaderAuthenticationFilterTest {

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    private GatewayHeaderAuthenticationFilter filter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        filter = new GatewayHeaderAuthenticationFilter(userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    private Utilizador buildUser() {
        Utilizador u = new Utente();
        u.setId(1L);
        u.setEmail("user@test.com");
        u.setNif("100000002");
        u.setNome("User");
        return u;
    }

    @Test
    void doFilterInternal_DeveIgnorarQuandoJaExisteAutenticacao() throws Exception {
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "existing", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertSame(auth, SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_DeveIgnorarQuandoNaoHaHeaderDeUser() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_DeveIgnorarQuandoGatewaySecretNaoBate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Authenticated-User", "user@test.com");
        request.addHeader("X-Gateway-Secret", "wrong-secret");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_NaoDeveFalharQuandoUserDetailsServiceLancaExcecao() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Authenticated-User", "user@test.com");
        request.addHeader("X-Gateway-Secret", "wrong-secret");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    @Test
    void parseAuthorities_DeveRetornarListaVaziaQuandoHeaderNull() throws Exception {
        Method method = GatewayHeaderAuthenticationFilter.class
                .getDeclaredMethod("parseAuthorities", String.class);
        method.setAccessible(true);

        Collection<SimpleGrantedAuthority> result =
                (Collection<SimpleGrantedAuthority>) method.invoke(filter, (String) null);

        assertTrue(result.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    void parseAuthorities_DeveRetornarAuthoritiesQuandoHeaderTemRoles() throws Exception {
        Method method = GatewayHeaderAuthenticationFilter.class
                .getDeclaredMethod("parseAuthorities", String.class);
        method.setAccessible(true);

        Collection<SimpleGrantedAuthority> result =
                (Collection<SimpleGrantedAuthority>) method.invoke(filter, "ROLE_SECRETARIA, ROLE_BALNEARIO");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SECRETARIA")));
        assertTrue(result.stream().anyMatch(a -> a.getAuthority().equals("ROLE_BALNEARIO")));
    }
}