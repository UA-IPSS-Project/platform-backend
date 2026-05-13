package pt.florinhas.api_gateway.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RateLimitFilterTest {

    private final RateLimitFilter filter = new RateLimitFilter();

    @Test
    void filter_DevePermitirRequestsDentroDoLimite() {

        MockServerWebExchange exchange =
                MockServerWebExchange.from(
                        MockServerHttpRequest
                                .get("/api/test")
                                .build());

        WebFilterChain chain = e -> Mono.empty();

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        assertNull(exchange.getResponse().getStatusCode());
    }
    @Test
    void filter_DeveAdicionarHeadersRateLimit() {

        MockServerWebExchange exchange =
                MockServerWebExchange.from(
                        MockServerHttpRequest
                                .get("/api/test")
                                .build());

        WebFilterChain chain = e -> Mono.empty();

        filter.filter(exchange, chain).block();

        assertNotNull(
                exchange.getResponse()
                        .getHeaders()
                        .getFirst("X-RateLimit-Limit")
        );

        assertNotNull(
                exchange.getResponse()
                        .getHeaders()
                        .getFirst("X-RateLimit-Remaining")
        );
    }
}
