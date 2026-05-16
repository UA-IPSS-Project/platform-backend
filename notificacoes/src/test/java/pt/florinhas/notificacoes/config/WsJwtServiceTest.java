package pt.florinhas.notificacoes.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class WsJwtServiceTest {

    private WsJwtService service;

    @BeforeEach
    void setUp() throws Exception {

        service = new WsJwtService();

        String secret =
                "abcdefghijklmnopqrstuvwxyz123456";

        Field secretField =
                WsJwtService.class
                        .getDeclaredField("secret");

        secretField.setAccessible(true);

        secretField.set(service, secret);

        SecretKey key =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8));

        Field keyField =
                WsJwtService.class
                        .getDeclaredField("key");

        keyField.setAccessible(true);

        keyField.set(service, key);
    }

    @Test
    void parseClaims_DeveRetornarClaims() {

        String token =
                Jwts.builder()
                        .subject("teste")
                        .claim(
                                "roles",
                                List.of("ROLE_USER"))
                        .signWith(
                                Keys.hmacShaKeyFor(
                                        "abcdefghijklmnopqrstuvwxyz123456"
                                                .getBytes(
                                                        StandardCharsets.UTF_8)))
                        .compact();

        var claims =
                service.parseClaims(token);

        assertNotNull(claims);

        assertEquals(
                "teste",
                claims.getSubject());
    }

    @Test
    void init_DeveExecutarSemErro() {

        assertDoesNotThrow(
                () -> service.init());
    }
}