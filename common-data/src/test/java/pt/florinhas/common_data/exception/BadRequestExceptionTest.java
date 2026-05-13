package pt.florinhas.common_data.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BadRequestExceptionTest {

    @Test
    void constructor_DeveDefinirMensagem() {

        BadRequestException ex =
                new BadRequestException("Erro");

        assertEquals("Erro", ex.getMessage());
    }

    @Test
    void constructor_DeveDefinirMensagemECausa() {

        RuntimeException cause =
                new RuntimeException("Causa");

        BadRequestException ex =
                new BadRequestException(
                        "Erro",
                        cause
                );

        assertEquals("Erro", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}