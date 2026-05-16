package pt.florinhas.marcacoes.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BusinessRuleExceptionTest {

    @Test
    @DisplayName("Deve criar BusinessRuleException com mensagem")
    void deveCriarBusinessRuleException() {

        BusinessRuleException exception =
                new BusinessRuleException("Erro de negócio");

        assertNotNull(exception);

        assertEquals(
                "Erro de negócio",
                exception.getMessage()
        );
    }
}