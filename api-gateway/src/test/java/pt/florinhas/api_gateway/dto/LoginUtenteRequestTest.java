package pt.florinhas.api_gateway.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LoginUtenteRequestTest {

    @Test
    void constructor_DeveCriarRequest() {

        LoginUtenteRequest request =
                new LoginUtenteRequest(
                        "123456789",
                        "password"
                );

        assertEquals(
                "123456789",
                request.nif());

        assertEquals(
                "password",
                request.password());
    }
}