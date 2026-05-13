package pt.florinhas.api_gateway.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SetPasswordRequestTest {

    @Test
    void constructor_DeveCriarRequest() {

        SetPasswordRequest request =
                new SetPasswordRequest(
                        "password",
                        true
                );

        assertEquals(
                "password",
                request.password());

        assertTrue(
                request.termsAccepted());
    }
}