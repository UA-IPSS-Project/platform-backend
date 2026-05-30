package pt.florinhas.requisicoes.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_DeveDefinirMensagem() {

        ResourceNotFoundException ex =
                new ResourceNotFoundException(
                        "Nao encontrado");

        assertEquals(
                "Nao encontrado",
                ex.getMessage());
    }
}