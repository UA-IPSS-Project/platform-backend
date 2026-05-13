package pt.florinhas.common_data.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_DeveDefinirMensagem() {

        ResourceNotFoundException ex =
                new ResourceNotFoundException(
                        "Não encontrado"
                );

        assertEquals(
                "Não encontrado",
                ex.getMessage());
    }
}