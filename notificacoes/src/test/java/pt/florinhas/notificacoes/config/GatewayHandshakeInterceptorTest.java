package pt.florinhas.notificacoes.config;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

class GatewayHandshakeInterceptorTest {

    private GatewayHandshakeInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new GatewayHandshakeInterceptor();
    }

    @Test
    void beforeHandshake_DevePermitirHandshake() {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        WebSocketHandler handler = mock(WebSocketHandler.class);

        // Mock the HttpHeaders
        HttpHeaders headers = mock(HttpHeaders.class);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst("X-Authenticated-User")).thenReturn(null);
        when(headers.getFirst("Cookie")).thenReturn(null);

        boolean result = interceptor.beforeHandshake(
                request,
                response,
                handler,
                new HashMap<>());

        assertTrue(result);
    }
}