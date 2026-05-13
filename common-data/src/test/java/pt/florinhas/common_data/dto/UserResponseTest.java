package pt.florinhas.common_data.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserResponseTest {

    @Test
    void constructor_DeveCriarUserResponse() {

        UserResponse response =
                new UserResponse(
                        1L,
                        "teste@teste.com",
                        "Teste",
                        "SECRETARIA",
                        "123456789",
                        "912345678"
                );

        assertEquals(1L, response.id());
        assertEquals("teste@teste.com", response.email());
        assertEquals("Teste", response.nome());
        assertEquals("SECRETARIA", response.role());
        assertEquals("123456789", response.nif());
        assertEquals("912345678", response.telefone());
    }
}