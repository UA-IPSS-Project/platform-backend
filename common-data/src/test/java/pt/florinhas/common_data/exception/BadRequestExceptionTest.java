package pt.florinhas.common_data.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BadRequestExceptionTest {

    @Test
    void construtorMensagem_DeveFuncionar() {

        BadRequestException ex =
                new BadRequestException(
                        "Erro");

        assertEquals(
                "Erro",
                ex.getMessage());
    }

    @Test
    void construtorMensagemECausa_DeveFuncionar() {

        RuntimeException cause =
                new RuntimeException(
                        "Causa");

        BadRequestException ex =
                new BadRequestException(
                        "Erro",
                        cause);

        assertEquals(
                "Erro",
                ex.getMessage());

        assertEquals(
                cause,
                ex.getCause());
    }
}