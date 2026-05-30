package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

class WebSocketUserPrincipalTest {

    @Test
    void constructorComClaims_DeveRetornarRoles() {

        Claims claims =
                Jwts.claims()
                        .add("roles", List.of("ADMIN"))
                        .build();

        WebSocketUserPrincipal principal =
                new WebSocketUserPrincipal(
                        "nuno@test.com",
                        claims);

        assertEquals(
                "nuno@test.com",
                principal.getName());

        assertEquals(
                List.of("ADMIN"),
                principal.getRoles());

        assertNotNull(
                principal.getClaims());
    }

    @Test
    void constructorComRoles_DeveRetornarRoles() {

        WebSocketUserPrincipal principal =
                new WebSocketUserPrincipal(
                        "nuno@test.com",
                        List.of("USER"));

        assertEquals(
                List.of("USER"),
                principal.getRoles());
    }

    @Test
    void getRoles_DeveRetornarListaVaziaQuandoClaimsNull() {

        WebSocketUserPrincipal principal =
                new WebSocketUserPrincipal(
                        "nuno@test.com",
                        (Claims) null);

        assertEquals(
                List.of(),
                principal.getRoles());
    }

    @Test
    void getRoles_DeveRetornarListaVaziaQuandoClaimInvalida() {

        Claims claims =
                Jwts.claims()
                        .add("roles", "ADMIN")
                        .build();

        WebSocketUserPrincipal principal =
                new WebSocketUserPrincipal(
                        "nuno@test.com",
                        claims);

        assertEquals(
                List.of(),
                principal.getRoles());
    }
}