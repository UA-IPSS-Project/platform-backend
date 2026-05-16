package pt.florinhas.notificacoes.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;

class WebSocketUserPrincipalTest {

    @Test
    void constructorComClaims_DeveRetornarDados() {

        var claims = Jwts.claims()
                .subject("teste")
                .add("roles", List.of("ROLE_USER"))
                .build();

        WebSocketUserPrincipal principal =
                new WebSocketUserPrincipal(
                        "teste",
                        claims);

        assertEquals(
                "teste",
                principal.getName());

        assertNotNull(
                principal.getClaims());

        assertEquals(
                1,
                principal.getRoles().size());
    }

    @Test
    void constructorComRoles_DeveRetornarRoles() {

        WebSocketUserPrincipal principal =
                new WebSocketUserPrincipal(
                        "teste",
                        List.of("ROLE_ADMIN"));

        assertEquals(
                "teste",
                principal.getName());

        assertEquals(
                "ROLE_ADMIN",
                principal.getRoles().get(0));
    }
}