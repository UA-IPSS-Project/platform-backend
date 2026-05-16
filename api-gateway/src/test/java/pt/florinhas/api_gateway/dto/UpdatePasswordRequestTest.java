package pt.florinhas.api_gateway.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UpdatePasswordRequestTest {

    @Test
    void constructor_DeveCriarRequest() {

        UpdatePasswordRequest request =
                new UpdatePasswordRequest(
                        "novapassword",
                        true
                );

        assertEquals(
                "novapassword",
                request.newPassword());

        assertTrue(
                request.termsAccepted());
    }
}