package pt.florinhas.candidaturas.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    private final String expectedGatewaySecret;

    public GatewayHeaderAuthenticationFilter(
            @Value("${gateway.shared-secret:}") String expectedGatewaySecret) {
        this.expectedGatewaySecret = expectedGatewaySecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = request.getHeader("X-Authenticated-User");
        if (!StringUtils.hasText(username)) {
            filterChain.doFilter(request, response);
            return;
        }

        String gatewaySecret = request.getHeader("X-Gateway-Secret");
        if (!StringUtils.hasText(gatewaySecret)
                || !StringUtils.hasText(expectedGatewaySecret)
                || !gatewaySecret.equals(expectedGatewaySecret)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized gateway origin");
            return;
        }

        Collection<? extends GrantedAuthority> authorities = parseAuthorities(request.getHeader("X-Authenticated-Roles"));

        User principal = new User(username, "", authorities);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                principal, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    private Collection<? extends GrantedAuthority> parseAuthorities(String rolesHeader) {
        if (!StringUtils.hasText(rolesHeader)) {
            return List.of();
        }
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
