package pt.florinhas.marcacoes.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.exception.BadRequestException;
import pt.florinhas.common_data.validation.NifValidator;

class NifValidatorTest {

    private NifValidator validator;

    @BeforeEach
    void setup() {
        validator = new NifValidator();
    }

    @Test
    void isFormatValid_DeveRetornarFalse_QuandoNull() {
        assertFalse(validator.isFormatValid(null));
    }

    @Test
    void isFormatValid_DeveRetornarFalse_QuandoNaoTem9Digitos() {
        assertFalse(validator.isFormatValid("123"));
    }

    @Test
    void isFormatValid_DeveRetornarFalse_QuandoTemLetras() {
        assertFalse(validator.isFormatValid("12345678A"));
    }

    @Test
    void isFormatValid_DeveRetornarTrue_QuandoFormatoCorreto() {
        assertTrue(validator.isFormatValid("100000002"));
    }

    @Test
    void isValidRequired_DeveRetornarFalse_QuandoPrefixoInvalido() {
        assertFalse(validator.isValidRequired("745618331"));
    }

    @Test
    void isValidRequired_DeveRetornarFalse_QuandoTodosDigitosIguais() {
        assertFalse(validator.isValidRequired("111111111"));
    }

    @Test
    void isValidRequired_DeveRetornarFalse_QuandoChecksumInvalido() {
        assertFalse(validator.isValidRequired("100000003"));
    }

    @Test
    void isValidRequired_DeveRetornarTrue_QuandoValido() {
        assertTrue(validator.isValidRequired("100000002"));
    }

    @Test
    void isValidOptional_DeveRetornarTrue_QuandoNull() {
        assertTrue(validator.isValidOptional(null));
    }

    @Test
    void isValidOptional_DeveRetornarTrue_QuandoVazio() {
        assertTrue(validator.isValidOptional(""));
    }

    @Test
    void isValidOptional_DeveDelegarParaRequired() {
        assertFalse(validator.isValidOptional("100000003"));
    }

    @Test
    void validateRequiredOrThrow_DeveFalhar_QuandoNull() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> validator.validateRequiredOrThrow(null)
        );
        assertEquals("NIF é obrigatório", ex.getMessage());
    }

    @Test
    void validateRequiredOrThrow_DeveFalhar_QuandoFormatoErrado() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> validator.validateRequiredOrThrow("123")
        );
        assertEquals("NIF deve conter exatamente 9 dígitos numéricos", ex.getMessage());
    }

    @Test
    void validateRequiredOrThrow_DeveFalhar_QuandoChecksumInvalido() {
        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> validator.validateRequiredOrThrow("100000003")
        );
        assertEquals("NIF inválido", ex.getMessage());
    }

    @Test
    void validateRequiredOrThrow_NaoDeveFalhar_QuandoValido() {
        assertDoesNotThrow(() -> validator.validateRequiredOrThrow("100000002"));
    }

    @Test
    void validateOptionalOrThrow_DeveIgnorarNull() {
        assertDoesNotThrow(() -> validator.validateOptionalOrThrow(null));
    }

    @Test
    void validateOptionalOrThrow_DeveFalhar_QuandoInvalido() {
        assertThrows(BadRequestException.class,
                () -> validator.validateOptionalOrThrow("100000003"));
    }

    @Test
    void validateOptionalOrThrow_NaoDeveFalhar_QuandoValido() {
        assertDoesNotThrow(() -> validator.validateOptionalOrThrow("100000002"));
    }
}