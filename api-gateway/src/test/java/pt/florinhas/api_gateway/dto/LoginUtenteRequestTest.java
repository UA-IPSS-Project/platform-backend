package pt.florinhas.api_gateway.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LoginUtenteRequestTest {

    @Test
    void loginUtenteRequest_DeveGuardarValores() {

        LoginUtenteRequest request =
                new LoginUtenteRequest(
                        "123456789",
                        "123");

        assertEquals(
                "123456789",
                request.nif());

        assertEquals(
                "123",
                request.password());
    }
}