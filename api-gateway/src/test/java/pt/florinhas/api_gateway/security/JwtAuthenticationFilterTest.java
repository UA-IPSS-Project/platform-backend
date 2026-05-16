package pt.florinhas.api_gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.server.WebFilterChain;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;

    private UserDetailsService userDetailsService;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {

        jwtService =
                org.mockito.Mockito.mock(
                        JwtService.class);

        userDetailsService =
                org.mockito.Mockito.mock(
                        UserDetailsService.class);

        filter =
                new JwtAuthenticationFilter(
                        jwtService,
                        userDetailsService,
                        "");

        TestUtils.setField(
                filter,
                "gatewaySharedSecret",
                "gateway-secret");
    }

    @Test
    void filter_DevePermitirPublicPath() {

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/auth/login")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        WebFilterChain chain =
                webExchange -> Mono.empty();

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void filter_DevePermitirOptions() {

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .method(
                                HttpMethod.OPTIONS,
                                "/api/private")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        WebFilterChain chain =
                webExchange -> Mono.empty();

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void filter_DeveRetornar401QuandoTokenAusente() {

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/private")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        WebFilterChain chain =
                webExchange -> Mono.empty();

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(
                401,
                exchange.getResponse()
                        .getStatusCode()
                        .value());

        assertEquals(
                MediaType.APPLICATION_JSON,
                exchange.getResponse()
                        .getHeaders()
                        .getContentType());
    }

    @Test
    void filter_DeveRetornar401QuandoJwtInvalido() {

        when(jwtService.parseClaims(anyString()))
                .thenThrow(
                        new RuntimeException(
                                "JWT inválido"));

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/private")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer token")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        WebFilterChain chain =
                webExchange -> Mono.empty();

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(
                401,
                exchange.getResponse()
                        .getStatusCode()
                        .value());
    }

    @Test
    void filter_DeveRetornar401QuandoSubjectAusente() {

        Claims claims =
                Jwts.claims()
                        .build();

        when(jwtService.parseClaims(anyString()))
                .thenReturn(claims);

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/private")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer token")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        WebFilterChain chain =
                webExchange -> Mono.empty();

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(
                401,
                exchange.getResponse()
                        .getStatusCode()
                        .value());
    }

    @Test
    void filter_DeveRetornar401QuandoUtilizadorNaoExiste() {

        Claims claims =
                Jwts.claims()
                        .subject("teste")
                        .build();

        when(jwtService.parseClaims(anyString()))
                .thenReturn(claims);

        when(userDetailsService.loadUserByUsername("teste"))
                .thenThrow(
                        new RuntimeException(
                                "User não encontrado"));

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/private")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer token")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        WebFilterChain chain =
                webExchange -> Mono.empty();

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(
                401,
                exchange.getResponse()
                        .getStatusCode()
                        .value());
    }

    @Test
    void filter_DeveAutenticarComBearerToken() {

        Claims claims =
                Jwts.claims()
                        .subject("teste")
                        .add(
                                "roles",
                                List.of("ROLE_USER"))
                        .add(
                                "userId",
                                1L)
                        .build();

        UserDetails user =
                User.withUsername("teste")
                        .password("password")
                        .authorities("ROLE_USER")
                        .build();

        when(jwtService.parseClaims(anyString()))
                .thenReturn(claims);

        when(userDetailsService.loadUserByUsername("teste"))
                .thenReturn(user);

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/private")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer token")
                        .remoteAddress(
                                new InetSocketAddress(
                                        "127.0.0.1",
                                        8080))
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        WebFilterChain chain =
                webExchange -> {

                    assertEquals(
                            "teste",
                            webExchange.getRequest()
                                    .getHeaders()
                                    .getFirst("X-Authenticated-User"));

                    assertEquals(
                            "1",
                            webExchange.getRequest()
                                    .getHeaders()
                                    .getFirst("X-Authenticated-User-Id"));

                    assertEquals(
                            "ROLE_USER",
                            webExchange.getRequest()
                                    .getHeaders()
                                    .getFirst("X-Authenticated-Roles"));

                    assertEquals(
                            "gateway-secret",
                            webExchange.getRequest()
                                    .getHeaders()
                                    .getFirst("X-Gateway-Secret"));

                    return Mono.empty();
                };

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void filter_DeveAutenticarViaCookie() {

        Claims claims =
                Jwts.claims()
                        .subject("cookie-user")
                        .build();

        UserDetails user =
                User.withUsername("cookie-user")
                        .password("password")
                        .authorities("ROLE_ADMIN")
                        .build();

        when(jwtService.parseClaims(anyString()))
                .thenReturn(claims);

        when(userDetailsService.loadUserByUsername("cookie-user"))
                .thenReturn(user);

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/private")
                        .cookie(
                                new HttpCookie(
                                        "jwt",
                                        "cookie-token"))
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        WebFilterChain chain =
                webExchange -> Mono.empty();

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void filter_DeveUsarAuthoritiesDoUserQuandoJwtNaoTemRoles() {

        Claims claims =
                Jwts.claims()
                        .subject("teste")
                        .build();

        UserDetails user =
                User.withUsername("teste")
                        .password("password")
                        .authorities("ROLE_MANAGER")
                        .build();

        when(jwtService.parseClaims(anyString()))
                .thenReturn(claims);

        when(userDetailsService.loadUserByUsername("teste"))
                .thenReturn(user);

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/private")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer token")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        WebFilterChain chain =
                webExchange -> {

                    assertEquals(
                            "ROLE_MANAGER",
                            webExchange.getRequest()
                                    .getHeaders()
                                    .getFirst("X-Authenticated-Roles"));

                    return Mono.empty();
                };

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void filter_DeveRemoverWWWAuthenticate() {

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/private")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        exchange.getResponse()
                .getHeaders()
                .add(
                        HttpHeaders.WWW_AUTHENTICATE,
                        "Basic");

        WebFilterChain chain =
                webExchange -> Mono.empty();

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(
                null,
                exchange.getResponse()
                        .getHeaders()
                        .getFirst(HttpHeaders.WWW_AUTHENTICATE));
    }

    @Test
    void filter_DeveDefinirContentTypeJson() {

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/private")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        WebFilterChain chain =
                webExchange -> Mono.empty();

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();

        assertNotNull(
                exchange.getResponse()
                        .getHeaders()
                        .getContentType());
    }
}