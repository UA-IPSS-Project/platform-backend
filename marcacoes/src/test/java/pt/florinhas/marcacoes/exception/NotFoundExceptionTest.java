package pt.florinhas.marcacoes.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotFoundExceptionTest {

    @Test
    @DisplayName("Deve criar NotFoundException com mensagem")
    void deveCriarNotFoundException() {

        NotFoundException exception =
                new NotFoundException("Não encontrado");

        assertNotNull(exception);

        assertEquals(
                "Não encontrado",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Deve criar NotFoundException com causa")
    void deveCriarNotFoundExceptionComCausa() {

        RuntimeException cause =
                new RuntimeException("Causa");

        NotFoundException exception =
                new NotFoundException("Erro", cause);

        assertNotNull(exception);

        assertEquals(
                "Erro",
                exception.getMessage()
        );

        assertEquals(
                cause,
                exception.getCause()
        );
    }
}