package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequisicaoEstadoTest {

    @Test
    void enum_DeveConterValoresEsperados() {

        assertEquals(
                RequisicaoEstado.ABERTO,
                RequisicaoEstado.valueOf("ABERTO"));

        assertEquals(
                RequisicaoEstado.EM_PROGRESSO,
                RequisicaoEstado.valueOf("EM_PROGRESSO"));

        assertEquals(
                RequisicaoEstado.FECHADO,
                RequisicaoEstado.valueOf("FECHADO"));

        assertEquals(
                RequisicaoEstado.RECUSADO,
                RequisicaoEstado.valueOf("RECUSADO"));
    }
}