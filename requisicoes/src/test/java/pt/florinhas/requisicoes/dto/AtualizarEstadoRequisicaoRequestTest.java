package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pt.florinhas.requisicoes.domain.RequisicaoEstado;

class AtualizarEstadoRequisicaoRequestTest {

    @Test
    void constructorAndAccessors_DeveFuncionar() {

        AtualizarEstadoRequisicaoRequest request =
                new AtualizarEstadoRequisicaoRequest(
                        RequisicaoEstado.EM_PROGRESSO);

        assertEquals(
                RequisicaoEstado.EM_PROGRESSO,
                request.estado());
    }
}