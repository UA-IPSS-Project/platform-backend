package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequisicaoTest {

    private static class TestRequisicao
            extends Requisicao {
    }

    @Test
    void onCreate_DeveInicializarCampos() {

        TestRequisicao requisicao =
                new TestRequisicao();

        requisicao.onCreate();

        assertEquals(
                RequisicaoEstado.ABERTO,
                requisicao.getEstado());

        assertNotNull(
                requisicao.getCriadoEm());

        assertNotNull(
                requisicao.getUltimaAlteracaoEstadoEm());
    }
}