package pt.florinhas.notificacoes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import pt.florinhas.notificacoes.security.GatewayHeaderAuthenticationFilter;

/**
 * Classe de configuração de segurança da aplicação.
 * Centraliza toda a configuração do Spring Security:
 * - regras de autenticação e autorização
 * - política de sessões
 * - definição do bean PasswordEncoder
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Filtro responsável por validar JWTs presentes nos pedidos HTTP.
     * Normalmente é inserido antes do filtro de autenticação padrão do Spring
     * Security.
     */
    private final GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter;

    /**
     * Construtor com injeção de dependências.
     * param jwtAuthFilter filtro JWT personalizado
     */
    public SecurityConfig(GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter) {
        this.gatewayHeaderAuthenticationFilter = gatewayHeaderAuthenticationFilter;
    }

    /**
     * Define a cadeia de filtros de segurança (SecurityFilterChain).
     * Aqui são configurados:
     * - CSRF (desativado para APIs REST)
     * - CORS
     * - regras de autorização
     * - política de sessões (stateless)
     * - cabeçalhos HTTP
     * param http objeto HttpSecurity fornecido pelo Spring
     * return SecurityFilterChain construída
     */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)

                // Configuração de autorização dos pedidos HTTP
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/internal/**").authenticated()
                        .requestMatchers("/ws", "/ws/**", "/ws-notificacoes", "/ws-notificacoes/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())

                // Define que a aplicação não mantém estado de sessão (JWT-based auth)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .addFilterBefore(gatewayHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Desativa frameOptions (necessário, por exemplo, para H2 Console)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        // Constrói e devolve a configuração final de segurança
        return http.build();
    }

    /**
     * Define o encoder de passwords da aplicação.
     * BCrypt é recomendado por ser adaptativo e resistente a ataques de força
     * bruta.
     * return PasswordEncoder baseado em BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}