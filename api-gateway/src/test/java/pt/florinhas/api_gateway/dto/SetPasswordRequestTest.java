package pt.florinhas.api_gateway.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SetPasswordRequestTest {

    @Test
    void setPasswordRequest_DeveGuardarValores() {

        SetPasswordRequest request =
                new SetPasswordRequest(
                        "123456",
                        true);

        assertEquals(
                "123456",
                request.password());

        assertEquals(
                true,
                request.termsAccepted());
    }
}