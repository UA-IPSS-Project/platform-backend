package pt.florinhas.notificacoes.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
                        "secret");
    }

    @AfterEach
    void clearContext() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_DeveAutenticarInternal() throws Exception {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        FilterChain chain =
                mock(FilterChain.class);

        when(request.getRequestURI())
                .thenReturn("/api/internal/test");

        when(request.getHeader("X-Gateway-Secret"))
                .thenReturn("secret");

        filter.doFilter(
                request,
                response,
                chain);

        assertEquals(
                "INTERNAL_SERVICE",
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal());

        verify(chain)
                .doFilter(
                        request,
                        response);
    }

    @Test
    void doFilterInternal_DeveRejeitarInternalSemSecret() throws Exception {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        FilterChain chain =
                mock(FilterChain.class);

        when(request.getRequestURI())
                .thenReturn("/api/internal/test");

        filter.doFilter(
                request,
                response,
                chain);

        verify(response)
                .sendError(
                        401,
                        "Unauthorized internal origin");
    }

    @Test
    void doFilterInternal_DeveIgnorarQuandoJaAutenticado() throws Exception {

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "user",
                                null,
                                List.of()));

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        FilterChain chain =
                mock(FilterChain.class);

        when(request.getRequestURI())
                .thenReturn("/api/test");

        filter.doFilter(
                request,
                response,
                chain);

        verify(chain)
                .doFilter(
                        request,
                        response);
    }

    @Test
    void doFilterInternal_DeveIgnorarSemHeaderUser() throws Exception {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        FilterChain chain =
                mock(FilterChain.class);

        when(request.getRequestURI())
                .thenReturn("/api/test");

        when(request.getHeader("X-Authenticated-User"))
                .thenReturn(null);

        filter.doFilter(
                request,
                response,
                chain);

        verify(chain)
                .doFilter(
                        request,
                        response);
    }

    @Test
    void doFilterInternal_DeveRejeitarGatewaySemSecret() throws Exception {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        FilterChain chain =
                mock(FilterChain.class);

        when(request.getRequestURI())
                .thenReturn("/api/test");

        when(request.getHeader("X-Authenticated-User"))
                .thenReturn("teste@test.com");

        filter.doFilter(
                request,
                response,
                chain);

        verify(response)
                .sendError(
                        401,
                        "Unauthorized gateway origin");
    }

    @Test
    void doFilterInternal_DeveAutenticarGateway() throws Exception {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        FilterChain chain =
                mock(FilterChain.class);

        Utilizador user =
                new Utilizador();

        user.setEmail("teste@test.com");

        when(request.getRequestURI())
                .thenReturn("/api/test");

        when(request.getHeader("X-Authenticated-User"))
                .thenReturn("teste@test.com");

        when(request.getHeader("X-Gateway-Secret"))
                .thenReturn("secret");

        when(request.getHeader("X-Authenticated-Roles"))
                .thenReturn("ROLE_ADMIN");

        when(userDetailsService.loadUserByUsername(
                "teste@test.com"))
                .thenReturn(user);

        filter.doFilter(
                request,
                response,
                chain);

        assertEquals(
                user,
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal());

        verify(chain)
                .doFilter(
                        request,
                        response);
    }

    @Test
    void doFilterInternal_DeveRejeitarUserInvalido() throws Exception {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        FilterChain chain =
                mock(FilterChain.class);

        when(request.getRequestURI())
                .thenReturn("/api/test");

        when(request.getHeader("X-Authenticated-User"))
                .thenReturn("teste@test.com");

        when(request.getHeader("X-Gateway-Secret"))
                .thenReturn("secret");

        when(userDetailsService.loadUserByUsername(
                "teste@test.com"))
                .thenThrow(new RuntimeException());

        filter.doFilter(
                request,
                response,
                chain);

        verify(response)
                .sendError(
                        401,
                        "Invalid authenticated user");
    }
}