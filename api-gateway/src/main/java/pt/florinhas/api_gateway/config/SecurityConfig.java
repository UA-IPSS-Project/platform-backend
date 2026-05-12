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
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.XorServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.WebFilter;

import pt.florinhas.api_gateway.security.JwtAuthenticationFilter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        var csrfTokenRepository = CookieServerCsrfTokenRepository.withHttpOnlyFalse();

        // XorServerCsrfTokenRequestAttributeHandler masks the token to prevent
        // BREACH attacks and correctly handles the Mono<CsrfToken> subscription.
        var csrfHandler = new XorServerCsrfTokenRequestAttributeHandler();

        return http
                .csrf(csrf -> csrf
                    .csrfTokenRepository(csrfTokenRepository)
                    .csrfTokenRequestHandler(csrfHandler)
                    // Apply CSRF only to state-changing methods AND exclude auth endpoints
                    .requireCsrfProtectionMatcher(new AndServerWebExchangeMatcher(
                        new OrServerWebExchangeMatcher(
                            new PathPatternParserServerWebExchangeMatcher("/**", HttpMethod.POST),
                            new PathPatternParserServerWebExchangeMatcher("/**", HttpMethod.PUT),
                            new PathPatternParserServerWebExchangeMatcher("/**", HttpMethod.DELETE),
                            new PathPatternParserServerWebExchangeMatcher("/**", HttpMethod.PATCH)
                        ),
                        new NegatedServerWebExchangeMatcher(
                            new OrServerWebExchangeMatcher(
                                new PathPatternParserServerWebExchangeMatcher("/api/auth/login/**"),
                                new PathPatternParserServerWebExchangeMatcher("/api/auth/register/**"),
                                new PathPatternParserServerWebExchangeMatcher("/api/auth/logout"),
                                new PathPatternParserServerWebExchangeMatcher("/actuator/**")
                            )
                        )
                    )))
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
                    .pathMatchers("/api/auth/login/**", "/api/auth/register/**", "/api/auth/logout",
                                  "/actuator/health", "/api/utilizadores/terms-content",
                                  "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**")
                    .permitAll()
                    .anyExchange()
                    .authenticated())
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    /**
     * Required in WebFlux: subscribes the CsrfToken Mono on every request so the
     * XSRF-TOKEN cookie is written to the response. Without this, the cookie is
     * never set and subsequent requests fail CSRF validation.
     *
     * See: https://docs.spring.io/spring-security/reference/reactive/exploits/csrf.html
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public WebFilter csrfTokenSubscribingFilter() {
        return (exchange, chain) -> chain.filter(exchange).doOnSuccess(v -> {
            Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                csrfToken.subscribe();
            }
        });
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter websocketAuthHeaderPropagator() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String upgrade = request.getHeaders().getUpgrade();
            if (upgrade != null && "websocket".equalsIgnoreCase(upgrade)) {
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
