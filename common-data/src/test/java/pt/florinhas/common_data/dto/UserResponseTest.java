package pt.florinhas.common_data.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UserResponseTest {

    @Test
    void userResponse_DeveGuardarValores() {

        UserResponse response =
                new UserResponse(
                        1L,
                        "teste@teste.com",
                        "Teste",
                        "UTENTE",
                        "123456789",
                        "912345678");

        assertEquals(1L, response.id());
        assertEquals("teste@teste.com", response.email());
        assertEquals("Teste", response.nome());
        assertEquals("UTENTE", response.role());
        assertEquals("123456789", response.nif());
        assertEquals("912345678", response.telefone());
    }
}