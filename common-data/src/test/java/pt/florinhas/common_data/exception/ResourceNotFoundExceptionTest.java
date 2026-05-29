package pt.florinhas.common_data.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_DeveGuardarMensagem() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "Não encontrado");

        assertEquals(
                "Não encontrado",
                exception.getMessage());
    }
}