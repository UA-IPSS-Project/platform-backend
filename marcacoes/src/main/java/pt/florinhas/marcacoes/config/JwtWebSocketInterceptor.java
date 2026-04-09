package pt.florinhas.marcacoes.config;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import java.util.List;

import pt.florinhas.marcacoes.security.JwtService;

@Component

public class JwtWebSocketInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;

    @Autowired
    public JwtWebSocketInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        // If user is already set (e.g. from handshake authentication), do nothing
        if (accessor.getUser() != null) {
            return message;
        }

        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String token = authHeaders.get(0);
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            try {
                Claims claims = jwtService.parseClaims(token);
                String subject = claims.getSubject();
                accessor.setUser(new WebSocketUserPrincipal(subject, claims));
            } catch (Exception e) {
                // Invalid token, do not set user
            }
        } else {
        }
        return message;
    }
}
