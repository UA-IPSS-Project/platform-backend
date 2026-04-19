package pt.florinhas.notificacoes.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component

public class JwtWebSocketInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        // If user is already set (e.g. from handshake authentication), do nothing
        if (accessor.getUser() != null) {
            return message;
        }

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object gatewayUser = sessionAttributes.get("gatewayUser");
            if (gatewayUser instanceof String user && StringUtils.hasText(user)) {
                @SuppressWarnings("unchecked")
                List<String> gatewayRoles = (List<String>) sessionAttributes.getOrDefault("gatewayRoles", List.of());
                accessor.setUser(new WebSocketUserPrincipal(user, gatewayRoles));
                return message;
            }
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            return null;
        }

        return message;
    }
}
