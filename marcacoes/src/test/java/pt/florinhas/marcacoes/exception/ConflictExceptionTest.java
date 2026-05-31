package pt.florinhas.marcacoes.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConflictExceptionTest {

    @Test
    void conflictException_DeveGuardarMensagem() {

        ConflictException ex = new ConflictException("Teste");

        assertEquals("Teste", ex.getMessage());
    }
}