package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;

class CriarManutencaoItemRequestTest
        extends BaseValidatorTest {

    @Test
    void devePassarQuandoDadosValidos() {

        CriarManutencaoItemRequest request =
                new CriarManutencaoItemRequest(
                        "CATL",
                        "Sala A",
                        "Janela");

        Set<ConstraintViolation<CriarManutencaoItemRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void deveFalharQuandoCategoriaVazia() {

        CriarManutencaoItemRequest request =
                new CriarManutencaoItemRequest(
                        "",
                        "Sala",
                        "Item");

        Set<ConstraintViolation<CriarManutencaoItemRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void deveFalharQuandoEspacoVazio() {

        CriarManutencaoItemRequest request =
                new CriarManutencaoItemRequest(
                        "CATL",
                        "",
                        "Item");

        Set<ConstraintViolation<CriarManutencaoItemRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void deveFalharQuandoItemVerificacaoVazio() {

        CriarManutencaoItemRequest request =
                new CriarManutencaoItemRequest(
                        "CATL",
                        "Sala",
                        "");

        Set<ConstraintViolation<CriarManutencaoItemRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }
}