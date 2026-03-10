package pt.florinhas.marcacoes.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.florinhas.marcacoes.exception.BadRequestException;
import pt.florinhas.marcacoes.service.nif.NifValidationService;

@ExtendWith(MockitoExtension.class)
class NifValidatorTest {

    @Mock
    private NifValidationService nifValidationService;

    @InjectMocks
    private NifValidator nifValidator;

    @Test
    void isValidRequired_DeveRetornarFalse_QuandoNifEInvalidoPorFormato() {
        assertFalse(nifValidator.isValidRequired(null));
        assertFalse(nifValidator.isValidRequired(""));
        assertFalse(nifValidator.isValidRequired("123"));
        assertFalse(nifValidator.isValidRequired("12345678"));
        assertFalse(nifValidator.isValidRequired("1234567890"));
        assertFalse(nifValidator.isValidRequired("12345A789"));

        // Para NIF inválido por formato, nunca deve chamar serviço externo
        verifyNoInteractions(nifValidationService);
    }

    @Test
    void isValidRequired_DeveRetornarFalse_QuandoServicoExternoRejeita() {
        String nif = "123456789";
        when(nifValidationService.validate(nif)).thenReturn(false);

        assertFalse(nifValidator.isValidRequired(nif));
    }

    @Test
    void isValidRequired_DeveRetornarFalse_QuandoNifTemNoveDigitosIguais() {
        String nifUm = "111111111";
        String nifDois = "222222222";

        assertFalse(nifValidator.isValidRequired(nifUm));
        assertFalse(nifValidator.isValidRequired(nifDois));

        // Regra local deve rejeitar antes de chamar o serviço externo
        verifyNoInteractions(nifValidationService);
    }

    @Test
    void isValidRequired_DeveRetornarFalse_QuandoPrefixoEInvalido() {
        // Prefixo 4 não está na lista permitida, apesar de o checksum estar correto.
        String nifPrefixoInvalido = "472289071";

        assertFalse(nifValidator.isValidRequired(nifPrefixoInvalido));
        verifyNoInteractions(nifValidationService);
    }

    @Test
    void isValidRequired_DeveRetornarTrue_QuandoNifEValido() {
        String nif = "272289078";
        when(nifValidationService.validate(nif)).thenReturn(true);

        assertTrue(nifValidator.isValidRequired(nif));
    }

    @Test
    void validateRequiredOrThrow_NaoDeveLancar_QuandoNifEValido() {
        String nif = "272289078";
        when(nifValidationService.validate(nif)).thenReturn(true);

        nifValidator.validateRequiredOrThrow(nif);
    }

    @Test
    void validateRequiredOrThrow_DeveLancarExcecao_QuandoNifEInvalido() {
        BadRequestException exNulo = assertThrows(BadRequestException.class,
                () -> nifValidator.validateRequiredOrThrow(null));
        assertTrue(exNulo.getMessage().contains("obrigatório"));

        BadRequestException exFormato = assertThrows(BadRequestException.class,
                () -> nifValidator.validateRequiredOrThrow("12345A789"));
        assertTrue(exFormato.getMessage().contains("9 dígitos"));

        when(nifValidationService.validate("123456789")).thenReturn(false);
        BadRequestException exServico = assertThrows(BadRequestException.class,
                () -> nifValidator.validateRequiredOrThrow("123456789"));
        assertTrue(exServico.getMessage().contains("NIF inválido"));
    }
}
