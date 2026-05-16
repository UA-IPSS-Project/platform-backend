package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;

class AtualizarEstadoRequisicaoRequestTest
        extends BaseValidatorTest {

    @Test
    void devePassarQuandoEstadoValido() {

        AtualizarEstadoRequisicaoRequest request =
                new AtualizarEstadoRequisicaoRequest(
                        RequisicaoEstado.FECHADO);

        Set<ConstraintViolation<AtualizarEstadoRequisicaoRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void deveFalharQuandoEstadoNull() {

        AtualizarEstadoRequisicaoRequest request =
                new AtualizarEstadoRequisicaoRequest(
                        null);

        Set<ConstraintViolation<AtualizarEstadoRequisicaoRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }
}