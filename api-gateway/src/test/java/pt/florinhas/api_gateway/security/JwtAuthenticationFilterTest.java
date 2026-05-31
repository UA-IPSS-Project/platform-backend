package pt.florinhas.api_gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.server.WebFilterChain;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import pt.florinhas.common_data.domain.Utilizador;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private UserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(UserDetailsService.class);

        filter = new JwtAuthenticationFilter(
                jwtService,
                userDetailsService,
                "secret");
    }

    @Test
    void filter_DeveIgnorarOptionsRequest() {

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .method(HttpMethod.OPTIONS, "/api/test")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        WebFilterChain chain =
                e -> Mono.empty();

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void filter_DeveRetornarUnauthorizedSemToken() {

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/private")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        WebFilterChain chain =
                e -> Mono.empty();

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
                        .subject("teste@teste.com")
                        .add("roles", List.of("ROLE_UTENTE"))
                        .add("userId", 1L)
                        .build();

        UserDetails user =
                org.springframework.security.core.userdetails.User
                        .withUsername("teste@teste.com")
                        .password("123")
                        .authorities(
                                new SimpleGrantedAuthority("ROLE_UTENTE"))
                        .build();

        when(jwtService.parseClaims("token"))
                .thenReturn(claims);

        when(userDetailsService.loadUserByUsername("teste@teste.com"))
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
                e -> {

                    assertEquals(
                            "teste@teste.com",
                            e.getRequest()
                                    .getHeaders()
                                    .getFirst("X-Authenticated-User"));

                    return Mono.empty();
                };

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void filter_DeveAutenticarComCookieJwt() {

        Claims claims =
                Jwts.claims()
                        .subject("teste")
                        .build();

        UserDetails user =
                org.springframework.security.core.userdetails.User
                        .withUsername("teste")
                        .password("123")
                        .authorities("ROLE_UTENTE")
                        .build();

        when(jwtService.parseClaims("token"))
                .thenReturn(claims);

        when(userDetailsService.loadUserByUsername("teste"))
                .thenReturn(user);

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/private")
                        .cookie(new HttpCookie("jwt", "token"))
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        WebFilterChain chain =
                e -> Mono.empty();

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();
    }
    
       @Test
        void filter_DeveRetornar401QuandoTokenInvalido() {

        when(jwtService.parseClaims("token"))
                .thenThrow(new RuntimeException("jwt inválido"));

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/private")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer token")
                        .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain =
                e -> Mono.error(
                        new IllegalStateException(
                                "chain não devia ser executada"));

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
                        .subject("teste@teste.com")
                        .build();

        when(jwtService.parseClaims("token"))
                .thenReturn(claims);

        when(userDetailsService.loadUserByUsername(
                "teste@teste.com"))
                .thenThrow(
                        new RuntimeException(
                                "user não encontrado"));

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
                e -> Mono.error(
                        new IllegalStateException(
                                "chain não devia ser executada"));

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
        void filter_DeveRetornar401QuandoSubjectVazio() {

        Claims claims =
                Jwts.claims()
                        .subject("")
                        .build();

        when(jwtService.parseClaims("token"))
                .thenReturn(claims);

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/private")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer token")
                        .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain =
                e -> Mono.error(
                        new IllegalStateException(
                                "chain não devia ser executada"));

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(
                401,
                exchange.getResponse()
                        .getStatusCode()
                        .value());
        }
}