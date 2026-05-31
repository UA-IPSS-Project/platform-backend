package pt.florinhas.marcacoes.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BusinessRuleExceptionTest {

    @Test
    void businessRuleException_DeveGuardarMensagem() {

        BusinessRuleException ex = new BusinessRuleException("Teste");

        assertEquals("Teste", ex.getMessage());
    }
}