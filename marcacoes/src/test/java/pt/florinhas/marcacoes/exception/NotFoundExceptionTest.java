package pt.florinhas.marcacoes.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NotFoundExceptionTest {

    @Test
    void notFoundException_DeveGuardarMensagem() {

        NotFoundException ex = new NotFoundException("Teste");

        assertEquals("Teste", ex.getMessage());
    }

    @Test
    void notFoundException_DeveGuardarCause() {

        RuntimeException cause = new RuntimeException("Cause");

        NotFoundException ex = new NotFoundException("Teste", cause);

        assertEquals("Teste", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}