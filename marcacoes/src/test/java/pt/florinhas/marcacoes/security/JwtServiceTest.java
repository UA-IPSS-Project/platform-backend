package pt.florinhas.marcacoes.security;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private JwtService jwtService;
    private String secret;

    @BeforeEach
    void setup() throws Exception {
        jwtService = new JwtService();
        secret = "12345678901234567890123456789012"; // 32 chars

        Field secretField = JwtService.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtService, secret);

        jwtService.init();
    }

    @Test
    void init_DeveInicializarKey() throws Exception {
        Field keyField = JwtService.class.getDeclaredField("key");
        keyField.setAccessible(true);

        Object keyValue = keyField.get(jwtService);

        assertNotNull(keyValue);
        assertTrue(keyValue instanceof SecretKey);
    }

    @Test
    void parseClaims_DeveRetornarClaimsValidas() {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject("user@test.com")
                .claim("role", "SECRETARIA")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        Claims claims = jwtService.parseClaims(token);

        assertEquals("user@test.com", claims.getSubject());
        assertEquals("SECRETARIA", claims.get("role", String.class));
    }

    @Test
    void parseClaims_DeveFalharQuandoTokenInvalido() {
        assertThrows(Exception.class,
                () -> jwtService.parseClaims("token.invalido.aqui"));
    }
}