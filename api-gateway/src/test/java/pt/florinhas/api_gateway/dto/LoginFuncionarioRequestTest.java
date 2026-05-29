package pt.florinhas.api_gateway.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LoginFuncionarioRequestTest {

    @Test
    void loginFuncionarioRequest_DeveGuardarValores() {

        LoginFuncionarioRequest request =
                new LoginFuncionarioRequest(
                        "teste@teste.com",
                        "123");

        assertEquals(
                "teste@teste.com",
                request.email());

        assertEquals(
                "123",
                request.password());
    }
}