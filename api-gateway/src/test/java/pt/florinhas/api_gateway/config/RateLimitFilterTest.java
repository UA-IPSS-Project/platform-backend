package pt.florinhas.api_gateway.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;

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
    @Test
        void filter_DeveBloquearQuandoLimiteExcedido() {

        WebFilterChain chain =
                mock(WebFilterChain.class);

        when(chain.filter(any()))
                .thenReturn(Mono.empty());

        for (int i = 0; i < 121; i++) {

                MockServerWebExchange exchange =
                        MockServerWebExchange.from(
                                MockServerHttpRequest
                                        .get("/api/test")
                                        .header(
                                                "X-Forwarded-For",
                                                "127.0.0.1"
                                        )
                                        .build()
                        );

                filter.filter(exchange, chain)
                        .block();

                if (i == 120) {

                assertEquals(
                        HttpStatus.TOO_MANY_REQUESTS,
                        exchange.getResponse()
                                .getStatusCode()
                );
                }
        }

        verify(chain, times(120))
                .filter(any());
        }
    
}
