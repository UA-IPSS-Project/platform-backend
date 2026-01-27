package pt.florinhas.marcacoes.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Filtro de autenticação JWT executado uma vez por request.
 *
 * Responsabilidades:
 * - Interceptar pedidos HTTP.
 * - Extrair e validar o token JWT do header Authorization.
 * - Carregar o utilizador associado ao token.
 * - Popular o SecurityContext com a autenticação válida.
 *
 * Extende OncePerRequestFilter para garantir execução única por request.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Método central do filtro, executado para cada request HTTP.
     *
     * Fluxo geral:
     * 1) Ignorar endpoints públicos de autenticação (login/registo).
     * 2) Ler o header Authorization.
     * 3) Extrair e validar o JWT.
     * 4) Carregar o utilizador e definir a autenticação no SecurityContext.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Caminho do endpoint atual
        String path = request.getServletPath();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        /**
         * Regra de bypass:
         * - Permite acesso sem JWT aos endpoints de login e registo.
         * - O endpoint /api/auth/me permanece protegido e requer JWT válido.
         */
        if (path.startsWith("/api/auth/")
                && !(path.equals("/api/auth/me") || path.startsWith("/api/auth/me/"))) {

            // Permitir login e registo sem autenticação prévia
            if (path.contains("/login") || path.contains("/register")) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Leitura do header Authorization
        final String authHeader = request.getHeader("Authorization");
        log.trace("DEBUG JWT: Header present? {}", (authHeader != null));
        final String jwt;
        final String userEmail;

        /**
         * Se não existir header Authorization ou não usar o esquema Bearer,
         * o filtro não tenta autenticar e deixa o pedido prosseguir.
         */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrai o token JWT removendo o prefixo "Bearer "
        jwt = authHeader.substring(7);

        // Extrai o username (email ou NIF) embutido no token
        userEmail = jwtService.extractUsername(jwt);
        log.trace("DEBUG JWT: Extracted UserEmail: {}", userEmail);

        /**
         * Apenas tenta autenticar se:
         * - O token contém um identificador válido
         * 2. O utilizador ainda não está autenticado no contexto
         */
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.trace("DEBUG JWT: Context Auth: {}", auth);

        // Verifica se o contexto está vazio OU se é uma autenticação anónima
        boolean isAnonymous = auth != null && auth.getPrincipal().equals("anonymousUser");

        if (userEmail != null && (auth == null || isAnonymous)) {
            // Carrega os detalhes do utilizador a partir do repositório
            UserDetails userDetails = null;
            try {
                log.trace("DEBUG JWT: Calling loadUserByUsername with {}", userEmail);
                if (this.userDetailsService == null)
                    log.error("DEBUG JWT: userDetailsService is NULL");
                userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                log.trace("DEBUG JWT: Loaded UserDetails for: {}", userDetails.getUsername());
            } catch (Throwable e) {
                log.error("DEBUG JWT: Error loading user", e);
            }

            if (userDetails != null) {
                boolean isValid = jwtService.isTokenValid(jwt, userDetails);
                log.trace("DEBUG JWT: Token valid? {}", isValid);

                // Valida o token contra o utilizador (assinatura, expiração, claims)
                if (isValid) {
                    log.debug("DEBUG JWT: Token is VALID for user {}", userDetails.getUsername());
                    // Cria o token de autenticação do Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    // Associa detalhes da request (IP, sessão, etc.)
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    // Define a autenticação no contexto global
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        // Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }
}
