package pt.florinhas.notificacoes.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import pt.florinhas.common_data.domain.Utilizador;

class GatewayHeaderAuthenticationFilterTest {

    private CustomUserDetailsService userDetailsService;

    private GatewayHeaderAuthenticationFilter filter;

    @BeforeEach
    void setUp() {

        userDetailsService =
                mock(CustomUserDetailsService.class);

        filter =
                new GatewayHeaderAuthenticationFilter(
                        userDetailsService,
                        "secret"
                );

        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_DeveAutenticarViaGateway()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "X-Authenticated-User",
                "teste@teste.com"
        );

        request.addHeader(
                "X-Gateway-Secret",
                "secret"
        );

        request.addHeader(
                "X-Authenticated-Roles",
                "ROLE_USER"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain =
                new MockFilterChain();

        Utilizador utilizador =
                new Utilizador();

        utilizador.setEmail(
                "teste@teste.com"
        );

        when(userDetailsService.loadUserByUsername(
                "teste@teste.com"))
                .thenReturn(utilizador);

        filter.doFilter(
                request,
                response,
                chain
        );

        assertNotNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void doFilterInternal_DeveRejeitarSecretInvalido()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                "X-Authenticated-User",
                "teste@teste.com"
        );

        request.addHeader(
                "X-Gateway-Secret",
                "wrong"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                new MockFilterChain()
        );

        assertEquals(
                401,
                response.getStatus()
        );
    }

    @Test
    void doFilterInternal_DevePermitirInternalComSecret()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI(
                "/api/internal/test"
        );

        request.addHeader(
                "X-Gateway-Secret",
                "secret"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                new MockFilterChain()
        );

        assertNotNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void doFilterInternal_DeveBloquearInternalSemSecret()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI(
                "/api/internal/test"
        );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                new MockFilterChain()
        );

        assertEquals(
                401,
                response.getStatus()
        );
    }
}