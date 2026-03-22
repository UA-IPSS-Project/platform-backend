package pt.florinhas.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

import pt.florinhas.api_gateway.security.JwtAuthenticationFilter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .authorizeExchange(auth -> auth.anyExchange().permitAll())
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    /**
     * Global filter to propagate Authorization header for WebSocket handshake requests.
     * This ensures the backend can extract the JWT for WebSocket authentication.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter websocketAuthHeaderPropagator() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String upgrade = request.getHeaders().getUpgrade();
            if (upgrade != null && "websocket".equalsIgnoreCase(upgrade)) {
                // Forward Authorization header if present
                String authHeader = request.getHeaders().getFirst("Authorization");
                if (authHeader != null) {
                    ServerWebExchange mutated = exchange.mutate()
                            .request(builder -> builder.header("Authorization", authHeader))
                            .build();
                    return chain.filter(mutated);
                }
            }
            return chain.filter(exchange);
        };
    }
}
