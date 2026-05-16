package pt.florinhas.api_gateway.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LoginFuncionarioRequestTest {

    @Test
    void constructor_DeveCriarRequest() {

        LoginFuncionarioRequest request =
                new LoginFuncionarioRequest(
                        "teste@teste.com",
                        "password"
                );

        assertEquals(
                "teste@teste.com",
                request.email());

        assertEquals(
                "password",
                request.password());
    }
}