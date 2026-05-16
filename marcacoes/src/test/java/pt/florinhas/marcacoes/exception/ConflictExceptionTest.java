package pt.florinhas.marcacoes.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConflictExceptionTest {

    @Test
    @DisplayName("Deve criar ConflictException com mensagem")
    void deveCriarConflictException() {

        ConflictException exception =
                new ConflictException("Conflito");

        assertNotNull(exception);

        assertEquals(
                "Conflito",
                exception.getMessage()
        );
    }
}