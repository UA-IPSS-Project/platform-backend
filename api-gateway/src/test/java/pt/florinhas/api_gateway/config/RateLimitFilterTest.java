package pt.florinhas.api_gateway.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
    }

    @Test
    void filter_DevePermitirGetNormal() {
        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/test")
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
    void filter_DeveAplicarRateLimitLogin() {
        WebFilterChain chain =
                e -> Mono.empty();

        for (int i = 0; i < 10; i++) {

            MockServerHttpRequest request =
                    MockServerHttpRequest
                            .post("/api/auth/login")
                            .build();

            MockServerWebExchange exchange =
                    MockServerWebExchange.from(request);

            StepVerifier.create(
                    filter.filter(exchange, chain))
                    .verifyComplete();
        }

        MockServerHttpRequest blockedRequest =
                MockServerHttpRequest
                        .post("/api/auth/login")
                        .build();

        MockServerWebExchange blockedExchange =
                MockServerWebExchange.from(blockedRequest);

        StepVerifier.create(
                filter.filter(blockedExchange, chain))
                .verifyComplete();

        assertEquals(
                HttpStatus.TOO_MANY_REQUESTS,
                blockedExchange.getResponse().getStatusCode());
    }

    @Test
    void filter_DeveAdicionarHeadersRateLimit() {
        MockServerHttpRequest request =
                MockServerHttpRequest
                        .post("/api/auth/login")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        WebFilterChain chain =
                e -> Mono.empty();

        StepVerifier.create(
                filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(
                "10",
                exchange.getResponse()
                        .getHeaders()
                        .getFirst("X-RateLimit-Limit"));
    }
}