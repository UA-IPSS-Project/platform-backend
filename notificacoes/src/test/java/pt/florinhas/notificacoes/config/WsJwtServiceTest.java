package pt.florinhas.notificacoes.config;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class WsJwtServiceTest {

    private WsJwtService service;

    private final String secret =
            "01234567890123456789012345678912";

    @BeforeEach
    void setUp() {

        service =
                new WsJwtService();

        ReflectionTestUtils.setField(
                service,
                "secret",
                secret
        );

        service.init();
    }

    @Test
    void parseClaims_DeveRetornarClaims() {

        String token =
                Jwts.builder()
                        .subject("teste")
                        .claim(
                                "roles",
                                List.of("ROLE_USER")
                        )
                        .issuedAt(new Date())
                        .signWith(
                                Keys.hmacShaKeyFor(
                                        secret.getBytes(
                                                StandardCharsets.UTF_8
                                        )
                                )
                        )
                        .compact();

        var claims =
                service.parseClaims(token);

        assertEquals(
                "teste",
                claims.getSubject()
        );
    }
}