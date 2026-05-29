package pt.florinhas.api_gateway.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RecoverPasswordRequestTest {

    @Test
    void recoverPasswordRequest_DeveGuardarValores() {

        RecoverPasswordRequest request =
                new RecoverPasswordRequest(
                        "teste@teste.com");

        assertEquals(
                "teste@teste.com",
                request.identifier());
    }
}