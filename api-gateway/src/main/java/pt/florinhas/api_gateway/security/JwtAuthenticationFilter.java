package pt.florinhas.api_gateway.security;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import pt.florinhas.common_data.domain.Utilizador;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private static final String MSG_TOKEN_INVALIDO = "Token inválido";
    private static final String HEADER_AUTH_USER_ID = "X-Authenticated-User-Id";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final String gatewaySharedSecret;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            @Value("${gateway.shared-secret:}") String gatewaySharedSecret) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.gatewaySharedSecret = gatewaySharedSecret;
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
            log.debug("JWT ausente para path={} method={}", path, method);
            return writeUnauthorized(exchange, "Token em falta");
        }

        Claims claims;
        try {
            claims = jwtService.parseClaims(token);
        } catch (Exception ex) {
            log.warn("JWT inválido para path={} method={}: {}", path, method, ex.getMessage());
            return writeUnauthorized(exchange, MSG_TOKEN_INVALIDO);
        }

        String subject = claims.getSubject();
        if (!StringUtils.hasText(subject)) {
            log.warn("JWT sem subject para path={} method={}", path, method);
            return writeUnauthorized(exchange, MSG_TOKEN_INVALIDO);
        }

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(subject);
        } catch (Exception ex) {
            log.warn("Utilizador do JWT não encontrado subject={} path={} method={}", subject, path, method);
            return writeUnauthorized(exchange, MSG_TOKEN_INVALIDO);
        }

        Collection<GrantedAuthority> authorities = extractAuthorities(claims, userDetails);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities);

        log.debug("JWT válido para subject={} path={} method={}", subject, path, method);

        // Build mutated request with trusted auth headers for downstream microservices
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> builder.headers(headers -> {
                    headers.remove("X-Authenticated-User");
                    headers.remove(HEADER_AUTH_USER_ID);
                    headers.remove("X-Authenticated-Roles");
                    headers.remove("X-Gateway-Secret");

                    headers.set("X-Authenticated-User", userDetails.getUsername());
                    headers.set("X-Gateway-Secret", gatewaySharedSecret);

                    Number userId = claims.get("userId", Number.class);
                    if (userId != null) {
                        headers.set(HEADER_AUTH_USER_ID, String.valueOf(userId.longValue()));
                    } else if (userDetails instanceof Utilizador u && u.getId() != null) {
                        headers.set(HEADER_AUTH_USER_ID, String.valueOf(u.getId()));
                    }

                    List<String> roles = authorities.stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList();
                    if (!roles.isEmpty()) {
                        headers.set("X-Authenticated-Roles", String.join(",", roles));
                    }
                }))
                .build();

        return chain.filter(mutatedExchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                    Mono.just(new SecurityContextImpl(authentication))));
    }

            @SuppressWarnings("unchecked")
            private Collection<GrantedAuthority> extractAuthorities(Claims claims, UserDetails userDetails) {
            List<String> tokenRoles = claims.get("roles", List.class);

            if (tokenRoles != null && !tokenRoles.isEmpty()) {
                return tokenRoles.stream()
                    .filter(StringUtils::hasText)
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                    .toList();
            }

            return new ArrayList<>(userDetails.getAuthorities());
            }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/register")
                || path.equals("/api/auth/logout")
                || path.startsWith("/actuator/health")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/webjars");
    }

    private String extractToken(ServerWebExchange exchange) {
        HttpCookie jwtCookie = exchange.getRequest().getCookies().getFirst("jwt");
        if (jwtCookie != null && StringUtils.hasText(jwtCookie.getValue())) {
            log.trace("Autenticação via cookie jwt");
            return jwtCookie.getValue();
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            log.trace("Autenticação via header Authorization");
            return authHeader.substring(7);
        }

        return null;
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().remove(HttpHeaders.WWW_AUTHENTICATE);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] payload = ("{\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(payload)));
    }
}
