package pt.florinhas.marcacoes.security;

import java.io.IOException;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro auxiliar para garantir que o token CSRF é gerado e enviado no Cookie.
 * Em Spring Security 6, o token é diferido (deferred) por padrão, ou seja,
 * só é gerado se for lido. Este filtro força a leitura para garantir que
 * o cookie XSRF-TOKEN é enviado ao frontend.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // In Spring Security 6+, CSRF token is deferred
        // We need to explicitly get it to force cookie generation
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            // Calling getToken() forces the cookie to be written
            csrfToken.getToken();
        }

        filterChain.doFilter(request, response);
    }
}
