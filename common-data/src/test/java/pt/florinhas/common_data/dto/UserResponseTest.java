package pt.florinhas.common_data.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UserResponseTest {

    @Test
    void record_DeveGuardarValores() {

        UserResponse response =
                new UserResponse(
                        1L,
                        "teste@email.com",
                        "Teste",
                        "ROLE_USER",
                        "123456789",
                        "912345678");

        assertEquals(1L, response.id());
        assertEquals("teste@email.com", response.email());
        assertEquals("Teste", response.nome());
        assertEquals("ROLE_USER", response.role());
        assertEquals("123456789", response.nif());
        assertEquals("912345678", response.telefone());
    }
}