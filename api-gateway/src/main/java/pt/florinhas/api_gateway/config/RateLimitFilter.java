package pt.florinhas.api_gateway.config;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import reactor.core.publisher.Mono;

/**
 * Rate limiter global baseado em Fixed Window Counter por IP.
 *
 * Limites:
 * - Endpoints de login/registo: 10 req/min (proteção contra brute-force)
 * - Todos os outros: 120 req/min
 *
 * Implementado como WebFilter (não GlobalFilter) para cobrir também os
 * endpoints locais do gateway (/api/auth/**).
 *
 * Buckets são mantidos em caches Caffeine com TTL de 2 minutos para evitar
 * crescimento ilimitado de memória com IPs únicos (bots, ataques).
 *
 * Responde com HTTP 429 e headers X-RateLimit-* conforme
 * RFC draft-ietf-httpapi-ratelimit-headers.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter implements WebFilter {

    private static final int LOGIN_LIMIT = 10;
    private static final int GLOBAL_LIMIT = 300;
    private static final long WINDOW_MS = 60_000L;

    private record Bucket(AtomicInteger count, long windowStart) {}

    // Caffeine caches with TTL — entries expire 2 min after last access,
    // bounding memory even under high volumes of unique IPs.
    private final Cache<String, Bucket> loginBuckets = Caffeine.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    private final Cache<String, Bucket> globalBuckets = Caffeine.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .maximumSize(50_000)
            .build();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if ("true".equalsIgnoreCase(System.getenv("DISABLE_RATE_LIMIT"))) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        boolean isAuthEndpoint = path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register");

        // Only rate-limit auth endpoints and state-changing methods (POST/PUT/DELETE/PATCH)
        // GET requests are not rate-limited to avoid blocking dashboard parallel loads
        if (!isAuthEndpoint && method.equals("GET")) {
            return chain.filter(exchange);
        }

        int limit = isAuthEndpoint ? LOGIN_LIMIT : GLOBAL_LIMIT;
        Cache<String, Bucket> cache = isAuthEndpoint ? loginBuckets : globalBuckets;

        String ip = getClientIp(exchange);

        Bucket bucket = cache.get(ip, k -> new Bucket(new AtomicInteger(0), Instant.now().toEpochMilli()));

        // Reset window if expired
        long now = Instant.now().toEpochMilli();
        if (now - bucket.windowStart() >= WINDOW_MS) {
            bucket = new Bucket(new AtomicInteger(0), now);
            cache.put(ip, bucket);
        }

        int current = bucket.count().incrementAndGet();
        int remaining = Math.max(0, limit - current);
        long retryAfter = Math.max(0, WINDOW_MS - (now - bucket.windowStart())) / 1000;

        // Use set() to avoid duplicate headers from retries/filters
        exchange.getResponse().getHeaders().set("X-RateLimit-Limit", String.valueOf(limit));
        exchange.getResponse().getHeaders().set("X-RateLimit-Remaining", String.valueOf(remaining));

        if (current > limit) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().set("Retry-After", String.valueOf(retryAfter));
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private String getClientIp(ServerWebExchange exchange) {
        // Only trust X-Forwarded-For/X-Real-IP when behind a known proxy (nginx).
        // In production, ensure these headers are stripped/overwritten by the proxy.
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        String realIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) return realIp;
        var addr = exchange.getRequest().getRemoteAddress();
        return addr != null ? addr.getAddress().getHostAddress() : "unknown";
    }
}
