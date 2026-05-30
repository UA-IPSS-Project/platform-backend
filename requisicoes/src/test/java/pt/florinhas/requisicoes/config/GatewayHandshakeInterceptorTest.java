package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        interceptor =
                new GatewayHandshakeInterceptor(
                        "secret");
    }

    @Test
    void beforeHandshake_DeveAceitarPedidoValido() {

        ServerHttpRequest request =
                mock(ServerHttpRequest.class);

        HttpHeaders headers =
                new HttpHeaders();

        headers.add(
                "X-Gateway-Secret",
                "secret");

        headers.add(
                "X-Authenticated-User",
                "nuno@test.com");

        headers.add(
                "X-Authenticated-Roles",
                "ADMIN,USER");

        when(request.getHeaders())
                .thenReturn(headers);

        Map<String, Object> attributes =
                new HashMap<>();

        boolean result =
                interceptor.beforeHandshake(
                        request,
                        mock(ServerHttpResponse.class),
                        mock(WebSocketHandler.class),
                        attributes);

        assertTrue(result);

        assertEquals(
                "nuno@test.com",
                attributes.get("gatewayUser"));

        assertEquals(
                List.of("ADMIN", "USER"),
                attributes.get("gatewayRoles"));
    }

    @Test
    void beforeHandshake_DeveFalharSemSecret() {

        ServerHttpRequest request =
                mock(ServerHttpRequest.class);

        HttpHeaders headers =
                new HttpHeaders();

        headers.add(
                "X-Authenticated-User",
                "nuno@test.com");

        when(request.getHeaders())
                .thenReturn(headers);

        boolean result =
                interceptor.beforeHandshake(
                        request,
                        mock(ServerHttpResponse.class),
                        mock(WebSocketHandler.class),
                        new HashMap<>());

        assertFalse(result);
    }

    @Test
    void beforeHandshake_DeveFalharSemUser() {

        ServerHttpRequest request =
                mock(ServerHttpRequest.class);

        HttpHeaders headers =
                new HttpHeaders();

        headers.add(
                "X-Gateway-Secret",
                "secret");

        when(request.getHeaders())
                .thenReturn(headers);

        boolean result =
                interceptor.beforeHandshake(
                        request,
                        mock(ServerHttpResponse.class),
                        mock(WebSocketHandler.class),
                        new HashMap<>());

        assertFalse(result);
    }
}