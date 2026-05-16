package pt.florinhas.notificacoes.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

class GatewayHandshakeInterceptorTest {

    private GatewayHandshakeInterceptor interceptor;

    @BeforeEach
    void setUp() {

        interceptor =
                new GatewayHandshakeInterceptor();
    }

    @Test
    void beforeHandshake_DevePermitirHandshake() {

        ServerHttpRequest request =
                org.mockito.Mockito.mock(
                        ServerHttpRequest.class);

        ServerHttpResponse response =
                org.mockito.Mockito.mock(
                        ServerHttpResponse.class);

        WebSocketHandler handler =
                org.mockito.Mockito.mock(
                        WebSocketHandler.class);

        boolean result =
                interceptor.beforeHandshake(
                        request,
                        response,
                        handler,
                        new HashMap<>());

        assertTrue(result);
    }
}