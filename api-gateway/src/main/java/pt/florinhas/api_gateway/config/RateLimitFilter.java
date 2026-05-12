package pt.florinhas.api_gateway.config;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Rate limiter global baseado em Token Bucket por IP.
 *
 * Limites:
 * - Endpoints de login/registo: 10 req/min (proteção contra brute-force)
 * - Todos os outros: 120 req/min
 *
 * Responde com HTTP 429 e headers X-RateLimit-* conforme RFC draft-ietf-httpapi-ratelimit-headers.
 */
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final int LOGIN_LIMIT = 10;
    private static final int GLOBAL_LIMIT = 120;
    private static final long WINDOW_MS = 60_000L;

    private record Bucket(AtomicInteger count, long windowStart) {}

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> globalBuckets = new ConcurrentHashMap<>();

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = getClientIp(exchange);
        String path = exchange.getRequest().getPath().value();

        boolean isAuthEndpoint = path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register");
        int limit = isAuthEndpoint ? LOGIN_LIMIT : GLOBAL_LIMIT;
        Map<String, Bucket> buckets = isAuthEndpoint ? loginBuckets : globalBuckets;

        Bucket bucket = buckets.compute(ip, (k, existing) -> {
            long now = Instant.now().toEpochMilli();
            if (existing == null || now - existing.windowStart() >= WINDOW_MS) {
                return new Bucket(new AtomicInteger(0), now);
            }
            return existing;
        });

        int current = bucket.count().incrementAndGet();
        int remaining = Math.max(0, limit - current);
        long retryAfter = Math.max(0, WINDOW_MS - (Instant.now().toEpochMilli() - bucket.windowStart())) / 1000;

        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(limit));
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(remaining));

        if (current > limit) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().add("Retry-After", String.valueOf(retryAfter));
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private String getClientIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        String realIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) return realIp;
        var addr = exchange.getRequest().getRemoteAddress();
        return addr != null ? addr.getAddress().getHostAddress() : "unknown";
    }
}
