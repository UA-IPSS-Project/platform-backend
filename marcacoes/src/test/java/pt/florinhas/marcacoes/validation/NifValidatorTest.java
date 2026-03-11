package pt.florinhas.marcacoes.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.exception.BadRequestException;

class NifValidatorTest {

    private final NifValidator nifValidator = new NifValidator();

    @Test
    void isValidRequired_DeveRetornarFalse_QuandoNifEInvalidoPorFormato() {
        assertFalse(nifValidator.isValidRequired(null));
        assertFalse(nifValidator.isValidRequired(""));
        assertFalse(nifValidator.isValidRequired("123"));
        assertFalse(nifValidator.isValidRequired("12345678"));
        assertFalse(nifValidator.isValidRequired("1234567890"));
        assertFalse(nifValidator.isValidRequired("12345A789"));
    }

    @Test
    void isValidRequired_DeveRetornarFalse_QuandoNifTemNoveDigitosIguais() {
        assertFalse(nifValidator.isValidRequired("111111111"));
        assertFalse(nifValidator.isValidRequired("222222222"));
    }

    @Test
    void isValidRequired_DeveRetornarFalse_QuandoPrefixoEInvalido() {
        // Prefixo 4 não está na lista permitida, apesar de o checksum estar correto.
        assertFalse(nifValidator.isValidRequired("472289071"));
    }

    @Test
    void isValidRequired_DeveRetornarTrue_QuandoNifEValido() {
        assertTrue(nifValidator.isValidRequired("272289078"));
    }

    @Test
    void validateRequiredOrThrow_NaoDeveLancar_QuandoNifEValido() {
        nifValidator.validateRequiredOrThrow("272289078");
    }

    @Test
    void validateRequiredOrThrow_DeveLancarExcecao_QuandoNifEInvalido() {
        BadRequestException exNulo = assertThrows(BadRequestException.class,
                () -> nifValidator.validateRequiredOrThrow(null));
        assertTrue(exNulo.getMessage().contains("obrigatório"));

        BadRequestException exFormato = assertThrows(BadRequestException.class,
                () -> nifValidator.validateRequiredOrThrow("12345A789"));
        assertTrue(exFormato.getMessage().contains("9 dígitos"));

        // NIF com checksum errado (último dígito alterado)
        BadRequestException exChecksum = assertThrows(BadRequestException.class,
                () -> nifValidator.validateRequiredOrThrow("272289079"));
        assertTrue(exChecksum.getMessage().contains("NIF inválido"));
    }
}
