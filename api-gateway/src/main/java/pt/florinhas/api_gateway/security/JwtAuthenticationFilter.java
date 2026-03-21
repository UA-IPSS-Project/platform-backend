package pt.florinhas.api_gateway.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (HttpMethod.OPTIONS.matches(request.getMethod()) || isPublicPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            writeUnauthorized(response, "Token em falta");
            return;
        }

        Claims claims;
        try {
            claims = jwtService.parseClaims(token);
        } catch (Exception ex) {
            writeUnauthorized(response, "Token inválido");
            return;
        }

        MutableHeaderHttpServletRequest wrapped = new MutableHeaderHttpServletRequest(request);
        wrapped.putHeader("X-Authenticated-User", claims.getSubject());
        Number userId = claims.get("userId", Number.class);
        if (userId != null) {
            wrapped.putHeader("X-Authenticated-User-Id", String.valueOf(userId.longValue()));
        }

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        if (roles != null && !roles.isEmpty()) {
            wrapped.putHeader("X-Authenticated-Roles", String.join(",", roles));
        }

        filterChain.doFilter(wrapped, response);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/login/")
                || path.startsWith("/api/auth/register/")
                || path.startsWith("/marcacoes/api/auth/login/")
                || path.startsWith("/marcacoes/api/auth/register/")
                || path.equals("/api/auth/logout");
    }

    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }

    private static final class MutableHeaderHttpServletRequest extends HttpServletRequestWrapper {
        private final Map<String, String> customHeaders = new HashMap<>();

        private MutableHeaderHttpServletRequest(HttpServletRequest request) {
            super(request);
        }

        private void putHeader(String name, String value) {
            customHeaders.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            String headerValue = customHeaders.get(name);
            if (headerValue != null) {
                return headerValue;
            }
            return ((HttpServletRequest) getRequest()).getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            List<String> values = new ArrayList<>();
            if (customHeaders.containsKey(name)) {
                values.add(customHeaders.get(name));
            }
            Enumeration<String> originalValues = ((HttpServletRequest) getRequest()).getHeaders(name);
            while (originalValues.hasMoreElements()) {
                values.add(originalValues.nextElement());
            }
            return Collections.enumeration(values);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(((HttpServletRequest) getRequest()).getHeaderNames());
            names.addAll(customHeaders.keySet());
            return Collections.enumeration(names);
        }
    }
}
