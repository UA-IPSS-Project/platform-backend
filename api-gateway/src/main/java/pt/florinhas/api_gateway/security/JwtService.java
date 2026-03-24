package pt.florinhas.api_gateway.security;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public String generateToken(AuthUserClaims user) {
        List<String> roles = user.roles().stream().map(GrantedAuthority::getAuthority).toList();

        String subject = (user.email() != null && !user.email().trim().isEmpty()) ? user.email() : user.nif();

        return Jwts.builder()
                .claims(Map.of(
                        "userId", user.userId(),
                        "email", user.email() != null ? user.email() : "",
                        "nome", user.nome(),
                        "role", user.role(),
                        "nif", user.nif() != null ? user.nif() : "",
                        "telefone", user.telefone() != null ? user.telefone() : "",
                        "roles", roles))
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getJwtExpiration() {
        return jwtExpiration;
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public record AuthUserClaims(
            Long userId,
            String email,
            String nome,
            String role,
            String nif,
            String telefone,
            Collection<? extends GrantedAuthority> roles) {
    }
}
