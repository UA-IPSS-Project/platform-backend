package pt.florinhas.marcacoes.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;

class WebSocketUserPrincipalTest {

    @Test
    void getRoles_DeveRetornarRoles() {
        Claims claims = mock(Claims.class);
        when(claims.get("roles")).thenReturn(List.of("ROLE_USER"));

        WebSocketUserPrincipal principal = new WebSocketUserPrincipal("nuno", claims);

        assertEquals(List.of("ROLE_USER"), principal.getRoles());
    }

    @Test
    void getRoles_DeveRetornarNullSeNaoForLista() {
        Claims claims = mock(Claims.class);
        when(claims.get("roles")).thenReturn("ROLE_USER");

        WebSocketUserPrincipal principal = new WebSocketUserPrincipal("nuno", claims);

        assertNull(principal.getRoles());
    }
}