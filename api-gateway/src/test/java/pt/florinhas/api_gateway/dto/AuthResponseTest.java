package pt.florinhas.api_gateway.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthResponseTest {

    @Test
    void constructor_DeveCriarAuthResponse() {

        AuthResponse response = new AuthResponse(
                1L,
                "teste@teste.com",
                "Teste",
                "UTENTE",
                "123456789",
                "912345678",
                999999L,
                true,
                false
        );

        assertEquals(1L, response.id());
        assertEquals("teste@teste.com", response.email());
        assertEquals("Teste", response.nome());
        assertEquals("UTENTE", response.role());
        assertEquals("123456789", response.nif());
        assertEquals("912345678", response.telefone());
        assertEquals(999999L, response.expiresAt());
        assertTrue(response.active());
        assertFalse(response.requiresPasswordSetup());
    }
}