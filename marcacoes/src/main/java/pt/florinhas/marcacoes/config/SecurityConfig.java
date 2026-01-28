package pt.florinhas.marcacoes.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import pt.florinhas.marcacoes.security.JwtAuthenticationFilter;

/**
 * Classe de configuração de segurança da aplicação.
 * Centraliza toda a configuração do Spring Security:
 * - regras de autenticação e autorização
 * - política de sessões
 * - configuração de CORS
 * - definição dos beans necessários para autenticação (AuthenticationManager,
 * AuthenticationProvider, PasswordEncoder)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins}")
    public String allowedOrigins;

    /**
     * Filtro responsável por validar JWTs presentes nos pedidos HTTP.
     * Normalmente é inserido antes do filtro de autenticação padrão do Spring
     * Security.
     */
    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Serviço que carrega os utilizadores da aplicação (ex.: a partir da base de
     * dados).
     * É usado pelo AuthenticationProvider para validar credenciais.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Construtor com injeção de dependências.
     * param jwtAuthFilter filtro JWT personalizado
     * param userDetailsService serviço de carregamento de utilizadores
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
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
                // Desativa proteção CSRF, adequada para APIs REST stateless
                .csrf(AbstractHttpConfigurer::disable)

                // Aplica a configuração de CORS definida no bean abaixo
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configuração de autorização dos pedidos HTTP
                .authorizeHttpRequests(auth -> auth
                        // Permitir endpoints de autenticação públicos
                        .requestMatchers("/api/auth/**").permitAll()
                        // Permitir OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Exigir autenticação para qualquer outro pedido
                        .anyRequest().authenticated())

                // Define que a aplicação não mantém estado de sessão (JWT-based auth)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Desativa frameOptions (necessário, por exemplo, para H2 Console)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        // Constrói e devolve a configuração final de segurança
        return http.build();
    }

    /**
     * Configuração global de CORS (Cross-Origin Resource Sharing).
     * Define:
     * - origens permitidas
     * - métodos HTTP permitidos
     * - cabeçalhos permitidos
     * - se credenciais podem ser incluídas
     * return CorsConfigurationSource a aplicar a todos os endpoints
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permite origens configuradas (split por vírgula)
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Permite todos os cabeçalhos
        configuration.setAllowedHeaders(List.of("*"));

        // Permite envio de cookies/credenciais
        configuration.setAllowCredentials(true);

        // Expõe o cabeçalho Authorization para o frontend
        configuration.setExposedHeaders(List.of("Authorization"));

        // Tempo máximo (em segundos) que o browser pode cachear a configuração CORS
        configuration.setMaxAge(3600L);

        // Aplica esta configuração a todos os endpoints da aplicação
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Define o AuthenticationProvider usado pelo Spring Security.
     * Utiliza:
     * - um UserDetailsService para carregar utilizadores
     * - um PasswordEncoder para validar passwords
     * return AuthenticationProvider configurado
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

        // Encoder usado para comparar passwords
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * Exposição do AuthenticationManager como bean.
     * Este objeto é usado, por exemplo, em endpoints de login para autenticar
     * manualmente um utilizador.
     * param config configuração automática do Spring Security
     * return AuthenticationManager configurado
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
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
