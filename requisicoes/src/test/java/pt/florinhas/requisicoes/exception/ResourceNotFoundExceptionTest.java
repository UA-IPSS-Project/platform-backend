package pt.florinhas.requisicoes.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_DeveGuardarMensagem() {

        ResourceNotFoundException ex =
                new ResourceNotFoundException("Teste");

        assertEquals("Teste", ex.getMessage());
    }
}