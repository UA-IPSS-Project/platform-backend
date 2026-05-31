package pt.florinhas.notificacoes.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class WsJwtServiceTest {

    private WsJwtService service;

    @BeforeEach
    void setUp() throws Exception {

        service = new WsJwtService();

        String secret = "12345678901234567890123456789012";

        setField(
                "secret",
                secret);

        SecretKey key =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8));

        setField(
                "key",
                key);
    }

    @Test
    void parseClaims_DeveRetornarClaims() throws Exception {

        String token =
                Jwts.builder()
                        .subject("nuno@test.com")
                        .claim(
                                "roles",
                                List.of("ADMIN"))
                        .signWith((SecretKey) getField("key"))
                        .compact();

        Claims claims =
                service.parseClaims(token);

        assertEquals(
                "nuno@test.com",
                claims.getSubject());
    }

    @Test
    void init_DeveCriarKey() throws Exception {

        WsJwtService jwtService =
                new WsJwtService();

        setField(
                jwtService,
                "secret",
                "12345678901234567890123456789012");

        jwtService.init();

        assertNotNull(
                getField(jwtService, "key"));
    }

    private void setField(
            String field,
            Object value)
            throws Exception {

        setField(
                service,
                field,
                value);
    }

    private void setField(
            Object target,
            String field,
            Object value)
            throws Exception {

        Field f =
                WsJwtService.class
                        .getDeclaredField(field);

        f.setAccessible(true);

        f.set(target, value);
    }

    private Object getField(String field)
            throws Exception {

        return getField(service, field);
    }

    private Object getField(
            Object target,
            String field)
            throws Exception {

        Field f =
                WsJwtService.class
                        .getDeclaredField(field);

        f.setAccessible(true);

        return f.get(target);
    }
}