package pt.florinhas.notificacoes.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
                new GatewayHandshakeInterceptor();
    }

    @Test
    void beforeHandshake_DeveGuardarHeadersGateway() {

        ServerHttpRequest request =
                mock(ServerHttpRequest.class);

        HttpHeaders headers =
                new HttpHeaders();

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
    void beforeHandshake_DeveGuardarJwtCookie() {

        ServerHttpRequest request =
                mock(ServerHttpRequest.class);

        HttpHeaders headers =
                new HttpHeaders();

        headers.add(
                "Cookie",
                "jwt=token123");

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
                "token123",
                attributes.get("jwt-token"));
    }

    @Test
    void beforeHandshake_DeveAceitarSemAuth() {

        ServerHttpRequest request =
                mock(ServerHttpRequest.class);

        when(request.getHeaders())
                .thenReturn(new HttpHeaders());

        boolean result =
                interceptor.beforeHandshake(
                        request,
                        mock(ServerHttpResponse.class),
                        mock(WebSocketHandler.class),
                        new HashMap<>());

        assertTrue(result);
    }

    @Test
    void afterHandshake_NaoDeveFalhar() {

        assertDoesNotThrow(() ->
                interceptor.afterHandshake(
                        mock(ServerHttpRequest.class),
                        mock(ServerHttpResponse.class),
                        mock(WebSocketHandler.class),
                        null));
    }
}