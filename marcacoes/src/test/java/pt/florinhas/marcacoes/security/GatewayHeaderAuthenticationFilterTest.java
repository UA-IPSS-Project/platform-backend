package pt.florinhas.marcacoes.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;

class GatewayHeaderAuthenticationFilterTest {

    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private FilterChain filterChain;

    private GatewayHeaderAuthenticationFilter filter;
    private final String secret = "test-secret";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        filter = new GatewayHeaderAuthenticationFilter(userDetailsService, secret);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar com sucesso quando headers corretos")
    void doFilterInternal_DeveAutenticarQuandoHeadersCorretos() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Authenticated-User", "user@test.com");
        request.addHeader("X-Gateway-Secret", secret);
        request.addHeader("X-Authenticated-Roles", "ROLE_USER");

        MockHttpServletResponse response = new MockHttpServletResponse();
        Utilizador user = new Utente();
        user.setEmail("user@test.com");

        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(user);

        filter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve ignorar quando Gateway Secret está incorreto")
    void doFilterInternal_DeveIgnorarQuandoSecretIncorreto() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Authenticated-User", "user@test.com");
        request.addHeader("X-Gateway-Secret", "wrong");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Classe deve carregar")
    void classeDeveCarregar() {
        assertNotNull(GatewayHeaderAuthenticationFilter.class);
    }
}