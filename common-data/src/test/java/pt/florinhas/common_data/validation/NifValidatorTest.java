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
    void normalize_DeveRemoverCaracteres() {

        String result =
                NifValidator.normalize(
                        " 123 456 789 ");

        assertEquals(
                "123456789",
                result);
    }

    @Test
    void normalize_DeveRetornarNull() {

        assertEquals(
                null,
                NifValidator.normalize(null));
    }

    @Test
    void isFormatValid_DeveValidarFormato() {

        assertTrue(
                validator.isFormatValid(
                        "123456789"));

        assertFalse(
                validator.isFormatValid(
                        "123"));

        assertFalse(
                validator.isFormatValid(
                        "abcdefghi"));
    }

    @Test
    void isValidRequired_DeveRetornarTrueParaNifValido() {

        assertTrue(
                validator.isValidRequired(
                        "123456789"));
    }

    @Test
    void isValidRequired_DeveRetornarFalseParaNifInvalido() {

        assertFalse(
                validator.isValidRequired(
                        "111111111"));
    }

    @Test
    void isValidOptional_DeveAceitarNull() {

        assertTrue(
                validator.isValidOptional(null));
    }

    @Test
    void validateRequiredOrThrow_DeveLancarExcecao() {

        assertThrows(
                BadRequestException.class,
                () -> validator.validateRequiredOrThrow(
                        "111111111"));
    }

    @Test
    void validateRequiredOrThrow_DeveAceitarValido() {

        assertDoesNotThrow(
                () -> validator.validateRequiredOrThrow(
                        "123456789"));
    }

    @Test
    void validateOptionalOrThrow_DeveAceitarNull() {

        assertDoesNotThrow(
                () -> validator.validateOptionalOrThrow(
                        null));
    }
}