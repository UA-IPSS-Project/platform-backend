package pt.florinhas.notificacoes.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;

class WebSocketUserPrincipalTest {

    @Test
    void getRoles_DeveRetornarRolesDiretas() {

        WebSocketUserPrincipal principal =
                new WebSocketUserPrincipal(
                        "teste",
                        List.of("ROLE_USER")
                );

        assertEquals(
                List.of("ROLE_USER"),
                principal.getRoles()
        );
    }

    @Test
    void getRoles_DeveRetornarRolesDosClaims() {

        Claims claims =
                mock(Claims.class);

        when(claims.get("roles"))
                .thenReturn(List.of("ROLE_ADMIN"));

        WebSocketUserPrincipal principal =
                new WebSocketUserPrincipal(
                        "teste",
                        claims
                );

        assertEquals(
                List.of("ROLE_ADMIN"),
                principal.getRoles()
        );
    }

    @Test
    void getName_DeveRetornarNome() {

        WebSocketUserPrincipal principal =
                new WebSocketUserPrincipal(
                        "teste",
                        List.of()
                );

        assertEquals(
                "teste",
                principal.getName()
        );
    }
}