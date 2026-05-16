package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

class CriarRequisicaoTransporteRequestTest
        extends BaseValidatorTest {

    @Test
    void devePassarQuandoDadosValidos() {

        CriarRequisicaoTransporteRequest request =
                new CriarRequisicaoTransporteRequest(
                        "Teste",
                        RequisicaoPrioridade.URGENTE,
                        1L,
                        "Aveiro",
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(2),
                        5,
                        "João",
                        List.of(1L),
                        null);

        Set<ConstraintViolation<CriarRequisicaoTransporteRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void deveFalharQuandoCondutorVazio() {

        CriarRequisicaoTransporteRequest request =
                new CriarRequisicaoTransporteRequest(
                        "Teste",
                        RequisicaoPrioridade.MEDIA,
                        1L,
                        "Aveiro",
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(1),
                        5,
                        "",
                        List.of(1L),
                        null);

        Set<ConstraintViolation<CriarRequisicaoTransporteRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void deveFalharQuandoNumeroPassageirosNegativo() {

        CriarRequisicaoTransporteRequest request =
                new CriarRequisicaoTransporteRequest(
                        "Teste",
                        RequisicaoPrioridade.MEDIA,
                        1L,
                        "Aveiro",
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(1),
                        -1,
                        "João",
                        List.of(1L),
                        null);

        Set<ConstraintViolation<CriarRequisicaoTransporteRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void deveFalharQuandoListaTransportesVazia() {

        CriarRequisicaoTransporteRequest request =
                new CriarRequisicaoTransporteRequest(
                        "Teste",
                        RequisicaoPrioridade.MEDIA,
                        1L,
                        "Aveiro",
                        LocalDateTime.now(),
                        LocalDateTime.now().plusHours(1),
                        2,
                        "João",
                        List.of(),
                        null);

        Set<ConstraintViolation<CriarRequisicaoTransporteRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }
}