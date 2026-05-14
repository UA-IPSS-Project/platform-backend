package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private ServerHttpRequest request;
    private ServerHttpResponse response;
    private WebSocketHandler handler;

    @BeforeEach
    void setUp() {
        interceptor = new GatewayHandshakeInterceptor("secret123");

        request = mock(ServerHttpRequest.class);
        response = mock(ServerHttpResponse.class);
        handler = mock(WebSocketHandler.class);
    }
    @Test
    void beforeHandshake_DeveRetornarTrueQuandoHeadersValidos() {

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Gateway-Secret", "secret123");
        headers.add("X-Authenticated-User", "ana");
        headers.add("X-Authenticated-Roles", "ADMIN, USER");

        when(request.getHeaders()).thenReturn(headers);

        Map<String, Object> attributes = new java.util.HashMap<>();

        boolean result = interceptor.beforeHandshake(
                request,
                response,
                handler,
                attributes);

        assertTrue(result);

        assertEquals("ana", attributes.get("gatewayUser"));

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) attributes.get("gatewayRoles");

        assertEquals(List.of("ADMIN", "USER"), roles);
    }

    @Test
    void beforeHandshake_DeveRetornarFalseQuandoSecretInvalido() {

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Gateway-Secret", "wrong-secret");
        headers.add("X-Authenticated-User", "ana");

        when(request.getHeaders()).thenReturn(headers);

        boolean result = interceptor.beforeHandshake(
                request,
                response,
                handler,
                new java.util.HashMap<>());

        assertFalse(result);
    }
    @Test
    void beforeHandshake_DeveRetornarFalseQuandoUserNaoExiste() {

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Gateway-Secret", "secret123");

        when(request.getHeaders()).thenReturn(headers);

        boolean result = interceptor.beforeHandshake(
                request,
                response,
                handler,
                new java.util.HashMap<>());

        assertFalse(result);
    }

    @Test
    void afterHandshake_NaoDeveLancarExcecao() {

        assertDoesNotThrow(() -> interceptor.afterHandshake(
                request,
                response,
                handler,
                null));
    }
}