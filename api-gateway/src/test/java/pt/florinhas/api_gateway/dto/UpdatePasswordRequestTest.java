package pt.florinhas.api_gateway.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UpdatePasswordRequestTest {

    @Test
    void updatePasswordRequest_DeveGuardarValores() {

        UpdatePasswordRequest request =
                new UpdatePasswordRequest(
                        "123456",
                        true);

        assertEquals(
                "123456",
                request.newPassword());

        assertEquals(
                true,
                request.termsAccepted());
    }
}