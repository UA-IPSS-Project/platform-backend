package pt.florinhas.marcacoes.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Stateless CSRF Token Repository usando Double Submit Cookie pattern.
 * 
 * Em vez de armazenar o token na sessão do servidor (stateful),
 * este repository armazena o token APENAS no cookie do cliente.
 * 
 * Segurança: O servidor valida que o token no cookie == token no header.
 * Como apenas JavaScript do mesmo domínio pode ler cookies,
 * isto previne ataques CSRF mesmo sem sessão do lado do servidor.
 * 
 * Compatível com arquiteturas stateless (JWT-based).
 */
@Slf4j
public class StatelessCsrfTokenRepository implements CsrfTokenRepository {

    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
    private static final String CSRF_PARAMETER_NAME = "_csrf";

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        // Gera um novo UUID como token CSRF
        String tokenValue = UUID.randomUUID().toString();
        log.debug("Generating NEW CSRF token: {}", tokenValue);
        return new DefaultCsrfToken(CSRF_HEADER_NAME, CSRF_PARAMETER_NAME, tokenValue);
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        // IMPORTANTE: Em modo stateless, NÃO apagar o cookie quando token é null
        // O Spring Security chama saveToken(null) em sessões stateless,
        // mas queremos que o token persista no cookie do cliente.
        // Apenas salvamos novos tokens, nunca apagamos.

        if (token == null) {
            log.debug("Ignoring null token save (stateless mode - cookie persists)");
            return; // NÃO apagar o cookie!
        }

        log.debug("Saving CSRF token to cookie: {}", token.getToken());

        // Usar ResponseCookie para controlar SameSite
        ResponseCookie cookie = ResponseCookie
                .from(CSRF_COOKIE_NAME, token.getToken())
                .secure(request.isSecure()) // true em HTTPS
                .path("/")
                .maxAge(-1) // Session cookie (Strict security policy)
                .httpOnly(false) // IMPORTANTE: Frontend precisa ler este cookie
                .sameSite("Lax") // Lax é seguro e permite navegação normal
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        // Ler token do cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (CSRF_COOKIE_NAME.equals(cookie.getName())) {
                    String tokenValue = cookie.getValue();
                    if (StringUtils.hasText(tokenValue)) {
                        log.debug("Loaded EXISTING CSRF token from cookie: {}", tokenValue);
                        return new DefaultCsrfToken(CSRF_HEADER_NAME, CSRF_PARAMETER_NAME, tokenValue);
                    }
                }
            }
        }
        log.debug("No CSRF token found in cookies");
        return null;
    }
}
