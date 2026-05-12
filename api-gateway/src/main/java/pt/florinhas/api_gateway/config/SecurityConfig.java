package pt.florinhas.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

import pt.florinhas.api_gateway.security.JwtAuthenticationFilter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                // CSRF: disabled intentionally.
                // Protection is provided by httpOnly JWT cookie + SameSite=Lax.
                // CookieServerCsrfTokenRepository blocks GETs in WebFlux when XSRF-TOKEN
                // cookie is absent, breaking /api/auth/me on page reload.
                // TODO: revisit with XorServerCsrfTokenRequestAttributeHandler before production.
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((exchange, denied) -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        exchange.getResponse().getHeaders().remove(HttpHeaders.WWW_AUTHENTICATE);
                        return exchange.getResponse().setComplete();
                    })
                    .accessDeniedHandler((exchange, denied) -> {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        exchange.getResponse().getHeaders().remove(HttpHeaders.WWW_AUTHENTICATE);
                        return exchange.getResponse().setComplete();
                    }))
                .authorizeExchange(auth -> auth
                    .pathMatchers("/api/auth/login/**", "/api/auth/register/**", "/api/auth/logout", "/actuator/health",
                                  "/api/utilizadores/terms-content")
                    .permitAll()
                    .anyExchange()
                    .authenticated())
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

        // Encoder usado para comparar passwords
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
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
