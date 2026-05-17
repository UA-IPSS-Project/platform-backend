package pt.florinhas.api_gateway.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Extrai claims do JWT Keycloak já validado pelo resource server e injeta
 * os headers internos esperados pelos microserviços downstream.
 *
 * Headers injetados:
 * - X-Authenticated-User      : email ou preferred_username
 * - X-Authenticated-User-Id   : sub (Keycloak user ID)
 * - X-Authenticated-Roles     : roles do claim 'roles' (realm roles), prefixadas com ROLE_
 * - X-Gateway-Secret          : segredo partilhado para validar origem
 */
@Component
@Order(-90)
public class KeycloakTokenFilter implements WebFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KeycloakTokenFilter.class);

    private final String gatewaySharedSecret;

    public KeycloakTokenFilter(@Value("${gateway.shared-secret:}") String gatewaySharedSecret) {
        this.gatewaySharedSecret = gatewaySharedSecret;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    var auth = ctx.getAuthentication();
                    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
                        return chain.filter(exchange);
                    }

                    String user = jwt.getClaimAsString("email");
                    if (user == null || user.isBlank()) {
                        user = jwt.getClaimAsString("preferred_username");
                    }

                    @SuppressWarnings("unchecked")
                    List<String> roles = jwt.getClaim("roles");
                    String rolesHeader = roles != null
                            ? String.join(",", roles.stream()
                                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                                    .toList())
                            : "";

                    log.info("KeycloakTokenFilter - User: {}, Roles Claim: {}, Formatted Roles Header: {}, All Claims: {}", 
                            user, jwt.getClaim("roles"), rolesHeader, jwt.getClaims());

                    final String finalUser = user != null ? user : "";
                    final String finalSub = jwt.getSubject() != null ? jwt.getSubject() : "";

                    ServerWebExchange mutated = exchange.mutate()
                            .request(r -> r.headers(h -> {
                                h.remove("X-Authenticated-User");
                                h.remove("X-Authenticated-User-Id");
                                h.remove("X-Authenticated-Roles");
                                h.remove("X-Gateway-Secret");
                                h.set("X-Authenticated-User", finalUser);
                                h.set("X-Authenticated-User-Id", finalSub);
                                h.set("X-Authenticated-Roles", rolesHeader);
                                h.set("X-Gateway-Secret", gatewaySharedSecret);
                            }))
                            .build();

                    return chain.filter(mutated);
                })
                .switchIfEmpty(chain.filter(exchange));
    }
}
