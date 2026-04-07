package pt.florinhas.requisicoes.config;

import io.jsonwebtoken.Claims;
import java.security.Principal;
import java.util.List;

public class WebSocketUserPrincipal implements Principal {
    private final String name;
    private final Claims claims;

    public WebSocketUserPrincipal(String name, Claims claims) {
        this.name = name;
        this.claims = claims;
    }

    @Override
    public String getName() {
        return name;
    }

    public Claims getClaims() {
        return claims;
    }

    public List<String> getRoles() {
        Object roles = claims.get("roles");
        if (roles instanceof List<?>) {
            return ((List<?>) roles).stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
        }
        return List.of();
    }
}
