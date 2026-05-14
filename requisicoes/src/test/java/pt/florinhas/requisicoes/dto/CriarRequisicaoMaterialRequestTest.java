package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

class CriarRequisicaoMaterialRequestTest
        extends BaseValidatorTest {

    @Test
    void devePassarQuandoDadosValidos() {

        CriarRequisicaoMaterialRequest.ItemMaterialRequest item =
                new CriarRequisicaoMaterialRequest
                        .ItemMaterialRequest(
                                1L,
                                5);

        CriarRequisicaoMaterialRequest request =
                new CriarRequisicaoMaterialRequest(
                        "Teste",
                        RequisicaoPrioridade.ALTA,
                        1L,
                        List.of(item));

        Set<ConstraintViolation<CriarRequisicaoMaterialRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void deveFalharQuandoPrioridadeNull() {

        CriarRequisicaoMaterialRequest request =
                new CriarRequisicaoMaterialRequest(
                        "Teste",
                        null,
                        1L,
                        List.of());

        Set<ConstraintViolation<CriarRequisicaoMaterialRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void deveFalharQuandoListaItensVazia() {

        CriarRequisicaoMaterialRequest request =
                new CriarRequisicaoMaterialRequest(
                        "Teste",
                        RequisicaoPrioridade.MEDIA,
                        1L,
                        List.of());

        Set<ConstraintViolation<CriarRequisicaoMaterialRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void deveFalharQuandoQuantidadeInvalida() {

        CriarRequisicaoMaterialRequest.ItemMaterialRequest item =
                new CriarRequisicaoMaterialRequest
                        .ItemMaterialRequest(
                                1L,
                                0);

        CriarRequisicaoMaterialRequest request =
                new CriarRequisicaoMaterialRequest(
                        "Teste",
                        RequisicaoPrioridade.ALTA,
                        1L,
                        List.of(item));

        Set<ConstraintViolation<CriarRequisicaoMaterialRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }
}