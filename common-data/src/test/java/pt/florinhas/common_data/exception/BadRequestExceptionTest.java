package pt.florinhas.common_data.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BadRequestExceptionTest {

    @Test
    void constructor_DeveGuardarMensagem() {

        BadRequestException exception =
                new BadRequestException(
                        "Erro");

        assertEquals(
                "Erro",
                exception.getMessage());
    }

    @Test
    void constructor_DeveGuardarCause() {

        RuntimeException cause =
                new RuntimeException("cause");

        BadRequestException exception =
                new BadRequestException(
                        "Erro",
                        cause);

        assertEquals(
                "Erro",
                exception.getMessage());

        assertEquals(
                cause,
                exception.getCause());
    }
}