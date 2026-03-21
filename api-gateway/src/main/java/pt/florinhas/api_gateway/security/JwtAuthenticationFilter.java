package pt.florinhas.api_gateway.security;

import java.nio.charset.StandardCharsets;
import java.util.List;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpCookie;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        HttpMethod method = exchange.getRequest().getMethod();

        if (HttpMethod.OPTIONS.equals(method) || isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange);
        if (!StringUtils.hasText(token)) {
            return writeUnauthorized(exchange, "Token em falta");
        }

        Claims claims;
        try {
            claims = jwtService.parseClaims(token);
        } catch (Exception ex) {
            return writeUnauthorized(exchange, "Token inválido");
        }

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> {
                    builder.header("X-Authenticated-User", claims.getSubject());

                    Number userId = claims.get("userId", Number.class);
                    if (userId != null) {
                        builder.header("X-Authenticated-User-Id", String.valueOf(userId.longValue()));
                    }

                    @SuppressWarnings("unchecked")
                    List<String> roles = claims.get("roles", List.class);
                    if (roles != null && !roles.isEmpty()) {
                        builder.header("X-Authenticated-Roles", String.join(",", roles));
                    }
                })
                .build();

        return chain.filter(mutatedExchange);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/login/")
                || path.startsWith("/api/auth/register/")
                || path.startsWith("/marcacoes/api/auth/login/")
                || path.startsWith("/marcacoes/api/auth/register/")
                || path.equals("/api/auth/logout");
    }

    private String extractToken(ServerWebExchange exchange) {
        HttpCookie jwtCookie = exchange.getRequest().getCookies().getFirst("jwt");
        if (jwtCookie != null && StringUtils.hasText(jwtCookie.getValue())) {
            return jwtCookie.getValue();
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] payload = ("{\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(payload)));
    }
}
