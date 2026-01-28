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

        // Leitura do Token dos Cookies
        String jwt = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwt_auth".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if (jwt == null) {
            // Fallback para Header apenas se necessário ou para debug (mas removido por
            // segurança como pedido)
            // filterChain.doFilter(request, response);
            // return;

            // Se não encontrou no cookie, verifica header só por compatibilidade legacy
            // (opcional, vou remover para forçar cookie)
            filterChain.doFilter(request, response);
            return;
        }

        // Extrai o username (email ou NIF) embutido no token
        // Nota: O método chama-se extractUsername mas retorna email/nif
        String userEmail = jwtService.extractUsername(jwt);

        /**
         * Apenas tenta autenticar se:
         * - O token contém um identificador válido
         * 2. O utilizador ainda não está autenticado no contexto
         */
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verifica se o contexto está vazio OU se é uma autenticação anónima
        boolean isAnonymous = auth != null && auth.getPrincipal().equals("anonymousUser");

        if (userEmail != null && (auth == null || isAnonymous)) {
            // Carrega os detalhes do utilizador a partir do repositório
            UserDetails userDetails = null;
            try {
                userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            } catch (Throwable e) {
                // Log silent or error
            }

            if (userDetails != null) {
                boolean isValid = jwtService.isTokenValid(jwt, userDetails);

                // Valida o token contra o utilizador (assinatura, expiração, claims)
                if (isValid) {
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
