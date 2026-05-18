package pt.florinhas.notificacoes.config;

import io.jsonwebtoken.Claims;
import java.security.Principal;
import java.util.List;

public class WebSocketUserPrincipal implements Principal {
    private final String name;
    private final Claims claims;
    private final List<String> roles;

    public WebSocketUserPrincipal(String name, Claims claims) {
        this.name = name;
        this.claims = claims;
        this.roles = List.of();
    }

    public WebSocketUserPrincipal(String name, List<String> roles) {
        this.name = name;
        this.claims = null;
        this.roles = roles != null ? roles : List.of();
    }

    @Override
    public String getName() {
        return name;
    }

    public Claims getClaims() {
        return claims;
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles() {
        if (!roles.isEmpty()) {
            return roles;
        }
        if (claims == null) {
            return List.of();
        }
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof List<?>) {
            return (List<String>) rolesClaim;
        }
        return List.of();
    }
}