package pt.florinhas.requisicoes.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

class WebSocketUserPrincipalTest {

    @Test
    void getName_DeveRetornarNome() {

        WebSocketUserPrincipal principal =
                new WebSocketUserPrincipal(
                        "ana",
                        List.of("ADMIN"));

        assertEquals(
                "ana",
                principal.getName());
    }

    @Test
    void getRoles_DeveRetornarRolesDoConstrutor() {

        WebSocketUserPrincipal principal =
                new WebSocketUserPrincipal(
                        "ana",
                        List.of("ADMIN", "USER"));

        assertEquals(
                List.of("ADMIN", "USER"),
                principal.getRoles());
    }

   @Test
        void getRoles_DeveRetornarRolesDosClaims() {

        Claims claims = mock(Claims.class);

        when(claims.get("roles"))
                .thenReturn(
                        List.of(
                                "GESTOR",
                                "FUNCIONARIO"));

        WebSocketUserPrincipal principal =
                new WebSocketUserPrincipal(
                        "ana",
                        claims);

        assertEquals(
                List.of(
                        "GESTOR",
                        "FUNCIONARIO"),
                principal.getRoles());
        }

        @Test
        void getRoles_DeveRetornarListaVaziaQuandoNaoExistemRoles() {

        Claims claims = mock(Claims.class);

        when(claims.get("roles"))
                .thenReturn(null);

        WebSocketUserPrincipal principal =
                new WebSocketUserPrincipal(
                        "ana",
                        claims);

        assertTrue(
                principal.getRoles().isEmpty());
        }
}