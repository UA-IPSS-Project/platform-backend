package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RequisicaoEstadoTest {

    @Test
    void values_DeveConterTodos() {

        assertEquals(
                4,
                RequisicaoEstado.values().length);

        assertEquals(
                RequisicaoEstado.ABERTO,
                RequisicaoEstado.valueOf("ABERTO"));

        assertEquals(
                RequisicaoEstado.RECUSADO,
                RequisicaoEstado.valueOf("RECUSADO"));
    }
}