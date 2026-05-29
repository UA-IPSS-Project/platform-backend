package pt.florinhas.common_data.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.exception.BadRequestException;

class NifValidatorTest {

    private NifValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NifValidator();
    }

    @Test
    void normalize_DeveRemoverCaracteresNaoNumericos() {

        String result =
                NifValidator.normalize(
                        " 123 456 789 ");

        assertEquals(
                "123456789",
                result);
    }

    @Test
    void normalize_DeveRetornarNullQuandoNull() {

        assertEquals(
                null,
                NifValidator.normalize(null));
    }

    @Test
    void normalize_DeveRetornarNullQuandoVazio() {

        assertEquals(
                null,
                NifValidator.normalize("   "));
    }

    @Test
    void isFormatValid_DeveRetornarTrue() {

        assertTrue(
                validator.isFormatValid(
                        "123456789"));
    }

    @Test
    void isFormatValid_DeveRetornarFalse() {

        assertFalse(
                validator.isFormatValid(
                        "123"));
    }

    @Test
    void isValidRequired_DeveRetornarTrue() {

        assertTrue(
                validator.isValidRequired(
                        "123456789"));
    }

    @Test
    void isValidRequired_DeveRetornarFalseQuandoFormatoInvalido() {

        assertFalse(
                validator.isValidRequired(
                        "123"));
    }

    @Test
    void isValidRequired_DeveRetornarFalseQuandoTodosDigitosIguais() {

        assertFalse(
                validator.isValidRequired(
                        "111111111"));
    }

    @Test
    void isValidOptional_DeveRetornarTrueQuandoNull() {

        assertTrue(
                validator.isValidOptional(null));
    }

    @Test
    void isValidOptional_DeveRetornarTrueQuandoBlank() {

        assertTrue(
                validator.isValidOptional(" "));
    }

    @Test
    void validateRequiredOrThrow_DeveAceitarNifValido() {

        assertDoesNotThrow(
                () -> validator.validateRequiredOrThrow(
                        "123456789"));
    }

    @Test
    void validateRequiredOrThrow_DeveFalharQuandoNull() {

        assertThrows(
                BadRequestException.class,
                () -> validator.validateRequiredOrThrow(
                        null));
    }

    @Test
    void validateRequiredOrThrow_DeveFalharQuandoFormatoInvalido() {

        assertThrows(
                BadRequestException.class,
                () -> validator.validateRequiredOrThrow(
                        "123"));
    }

    @Test
    void validateRequiredOrThrow_DeveFalharQuandoChecksumInvalido() {

        assertThrows(
                BadRequestException.class,
                () -> validator.validateRequiredOrThrow(
                        "123456780"));
    }

    @Test
    void validateOptionalOrThrow_DeveAceitarNull() {

        assertDoesNotThrow(
                () -> validator.validateOptionalOrThrow(
                        null));
    }

    @Test
    void validateOptionalOrThrow_DeveAceitarBlank() {

        assertDoesNotThrow(
                () -> validator.validateOptionalOrThrow(
                        " "));
    }

    @Test
    void validateOptionalOrThrow_DeveFalharQuandoInvalido() {

        assertThrows(
                BadRequestException.class,
                () -> validator.validateOptionalOrThrow(
                        "123"));
    }
}