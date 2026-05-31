package pt.florinhas.api_gateway.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@SuppressWarnings({"rawtypes", "unchecked"})
class AuditClientTest {

    private WebClient.Builder builder;
    private WebClient webClient;
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;

    private AuditClient auditClient;

    @BeforeEach
    void setUp() {

        builder = mock(WebClient.Builder.class);
        webClient = mock(WebClient.class);

        requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);

        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);

        responseSpec = mock(WebClient.ResponseSpec.class);

        when(builder.baseUrl(any()))
                .thenReturn(builder);

        when(builder.build())
                .thenReturn(webClient);

        when(webClient.post())
                .thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.uri(any(String.class)))
                .thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.header(any(), any()))
                .thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.bodyValue(any()))
                .thenReturn(requestHeadersSpec);

        when(requestHeadersSpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.toBodilessEntity())
                .thenReturn(Mono.empty());

        auditClient = new AuditClient(
                builder,
                "http://localhost",
                "secret");
    }

    @Test
    void logAsync_DeveEnviarPedidoAuditoria() {

        auditClient.logAsync(
                1L,
                "Nuno",
                "LOGIN",
                "UTILIZADOR",
                1L,
                "teste",
                "127.0.0.1");

        verify(webClient).post();

        verify(requestBodyUriSpec)
                .uri("/api/audit/internal/log");

        verify(requestBodyUriSpec)
                .header(
                        "X-Gateway-Secret",
                        "secret");
    }

    @Test
    void logAsync_DeveUsarValoresDefaultQuandoNull() {

        auditClient.logAsync(
                null,
                null,
                "LOGIN",
                null,
                null,
                null,
                null);

        verify(requestBodyUriSpec)
                .bodyValue(any());
    }
}