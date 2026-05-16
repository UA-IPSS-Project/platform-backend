package pt.florinhas.marcacoes.security;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;
    private final String expectedGatewaySecret;

    public GatewayHeaderAuthenticationFilter(CustomUserDetailsService userDetailsService,
            @Value("${gateway.shared-secret:}") String expectedGatewaySecret) {
        this.userDetailsService = userDetailsService;
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

        // Verify that the request comes from the trusted gateway by checking a shared secret header.
        String gatewaySecret = request.getHeader("X-Gateway-Secret");
        if (!StringUtils.hasText(gatewaySecret)
                || !StringUtils.hasText(expectedGatewaySecret)
                || !gatewaySecret.equals(expectedGatewaySecret)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized gateway origin");
            return;
        }
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Collection<? extends GrantedAuthority> authorities = parseAuthorities(request.getHeader("X-Authenticated-Roles"));
            if (authorities.isEmpty()) {
                authorities = userDetails.getAuthorities();
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    authorities);

            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authenticated user");
            return;
        }
        
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