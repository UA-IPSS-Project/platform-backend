package pt.florinhas.notificacoes.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.Map;

/**
 * Interceptor STOMP que autentica o utilizador no CONNECT.
 * Suporta dois modos de autenticação:
 * 1. Via sessão (headers injetados pelo GatewayHandshakeInterceptor)
 * 2. Via JWT no header Authorization do frame STOMP CONNECT (fallback direto)
 */
@Component
@Slf4j
public class JwtWebSocketInterceptor implements ChannelInterceptor {

    private final WsJwtService wsJwtService;

    public JwtWebSocketInterceptor(WsJwtService wsJwtService) {
        this.wsJwtService = wsJwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        // If user is already set (e.g. from handshake authentication), do nothing
        if (accessor.getUser() != null) {
            return message;
        }

        // 1. Try to get user from session attributes (set by GatewayHandshakeInterceptor)
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object gatewayUser = sessionAttributes.get("gatewayUser");
            if (gatewayUser instanceof String user && StringUtils.hasText(user)) {
                @SuppressWarnings("unchecked")
                List<String> gatewayRoles = (List<String>) sessionAttributes.getOrDefault("gatewayRoles", List.of());
                accessor.setUser(new WebSocketUserPrincipal(user, gatewayRoles));
                log.info("[JwtWsInterceptor] Authenticated via session attributes: {}", user);
                return message;
            }
        }

        // 2. Fallback: try to read JWT from STOMP CONNECT Authorization header or session attributes
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null) {
                authHeader = accessor.getFirstNativeHeader("jwt");
            }
            
            // If still null, check if we stored it in session attributes during handshake
            if (authHeader == null && sessionAttributes != null) {
                authHeader = (String) sessionAttributes.get("jwt-token");
            }

            if (StringUtils.hasText(authHeader)) {
                String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
                try {
                    Claims claims = wsJwtService.parseClaims(token);
                    String subject = claims.getSubject();
                    if (StringUtils.hasText(subject)) {
                        @SuppressWarnings("unchecked")
                        List<String> roles = claims.get("roles", List.class);
                        accessor.setUser(new WebSocketUserPrincipal(subject, roles != null ? roles : List.of()));
                        log.info("[JwtWsInterceptor] Authenticated via STOMP JWT header: {}", subject);
                        return message;
                    }
                } catch (Exception e) {
                    log.error("[JwtWsInterceptor] STOMP JWT validation failed: {}", e.getMessage());
                }
            }

            // No valid auth found — reject the CONNECT
            log.error("[JwtWsInterceptor] REJECTED STOMP CONNECT: No valid auth found");
            return null;
        }

        return message;
    }
}