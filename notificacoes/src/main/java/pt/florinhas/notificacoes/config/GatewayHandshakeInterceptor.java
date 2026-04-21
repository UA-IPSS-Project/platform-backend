package pt.florinhas.notificacoes.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor de handshake WebSocket.
 * Permite sempre a ligação WebSocket — a autenticação real é feita
 * pelo JwtWebSocketInterceptor ao nível do frame STOMP CONNECT,
 * onde o token JWT é lido diretamente dos headers STOMP.
 *
 * Se os headers X-Authenticated-User/X-Authenticated-Roles estiverem presentes
 * (injetados pelo Gateway), são colocados nos atributos de sessão como
 * método de autenticação alternativo mais eficiente.
 */
@Component
@Slf4j
public class GatewayHandshakeInterceptor implements HandshakeInterceptor {

    private static final String ATTR_USER = "gatewayUser";
    private static final String ATTR_ROLES = "gatewayRoles";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) {

        // Se o Gateway injetou os headers de autenticação, aproveitar
        String user = request.getHeaders().getFirst("X-Authenticated-User");
        if (StringUtils.hasText(user)) {
            attributes.put(ATTR_USER, user);
            String rolesHeader = request.getHeaders().getFirst("X-Authenticated-Roles");
            if (StringUtils.hasText(rolesHeader)) {
                List<String> roles = Arrays.stream(rolesHeader.split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .toList();
                attributes.put(ATTR_ROLES, roles);
            }
            log.info("[WS-Handshake] Authenticated via Gateway headers: {}", user);
        } else {
            // Caso contrário, tentar ler do cookie 'jwt' (bypass Gateway)
            String token = null;

            // 1. Tentar Cookie 'jwt'
            String cookieHeader = request.getHeaders().getFirst("Cookie");
            if (StringUtils.hasText(cookieHeader)) {
                token = Arrays.stream(cookieHeader.split(";"))
                        .map(String::trim)
                        .filter(s -> s.startsWith("jwt="))
                        .map(s -> s.substring(4))
                        .findFirst()
                        .orElse(null);
            }

            if (StringUtils.hasText(token)) {
                // Colocar o token nos atributos para o JwtWebSocketInterceptor o processar
                attributes.put("jwt-token", token);
                log.info("[WS-Handshake] Found JWT in cookie/query, passing to STOMP level");
            } else {
                log.info("[WS-Handshake] No auth found in handshake");
            }
        }

        // Sempre permite o handshake — a autenticação é delegada ao nível STOMP
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }
}