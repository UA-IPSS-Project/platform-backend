package pt.florinhas.marcacoes.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

/**
 * Filtro de autenticação JWT executado uma vez por request.
 *
 * Responsabilidades:
 *  - Interceptar pedidos HTTP.
 *  - Extrair e validar o token JWT do header Authorization.
 *  - Carregar o utilizador associado ao token.
 *  - Popular o SecurityContext com a autenticação válida.
 *
 * Extende OncePerRequestFilter para garantir execução única por request.
 */
@Component
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
     *  1) Ignorar endpoints públicos de autenticação (login/registo).
     *  2) Ler o header Authorization.
     *  3) Extrair e validar o JWT.
     *  4) Carregar o utilizador e definir a autenticação no SecurityContext.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Caminho do endpoint atual
        String path = request.getServletPath();

        /**
         * Regra de bypass:
         *  - Permite acesso sem JWT aos endpoints de login e registo.
         *  - O endpoint /api/auth/me permanece protegido e requer JWT válido.
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

        /**
         * Apenas tenta autenticar se:
         *  - O token contém um identificador válido
         *  - Ainda não existe autenticação no contexto de segurança
         */
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Carrega os detalhes do utilizador a partir do repositório
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            // Valida o token contra o utilizador (assinatura, expiração, claims)
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Cria o token de autenticação do Spring Security
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                // Associa detalhes da request (IP, sessão, etc.)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // Define a autenticação no contexto global
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }
}
