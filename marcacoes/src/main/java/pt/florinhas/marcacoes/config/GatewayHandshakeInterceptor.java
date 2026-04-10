package pt.florinhas.marcacoes.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class GatewayHandshakeInterceptor implements HandshakeInterceptor {

    private static final String ATTR_USER = "gatewayUser";
    private static final String ATTR_ROLES = "gatewayRoles";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String gatewaySecret = request.getHeaders().getFirst("X-Gateway-Secret");
        String expectedGatewaySecret = System.getenv("GATEWAY_SHARED_SECRET");
        if (!StringUtils.hasText(gatewaySecret)
                || !StringUtils.hasText(expectedGatewaySecret)
                || !gatewaySecret.equals(expectedGatewaySecret)) {
            return false;
        }

        String user = request.getHeaders().getFirst("X-Authenticated-User");
        if (!StringUtils.hasText(user)) {
            return false;
        }

        attributes.put(ATTR_USER, user);

        String rolesHeader = request.getHeaders().getFirst("X-Authenticated-Roles");
        if (StringUtils.hasText(rolesHeader)) {
            List<String> roles = Arrays.stream(rolesHeader.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .toList();
            attributes.put(ATTR_ROLES, roles);
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }
}