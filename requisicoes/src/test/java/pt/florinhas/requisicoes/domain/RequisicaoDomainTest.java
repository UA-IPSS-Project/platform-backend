package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class RequisicaoDomainTest {

    @Test
    void onCreate_quandoEstadoNulo_deveDefinirAbertoEDatas() {
        RequisicaoManutencao requisicao = new RequisicaoManutencao();

        requisicao.onCreate();

        assertEquals(RequisicaoEstado.ABERTO, requisicao.getEstado());
        assertNotNull(requisicao.getCriadoEm());
        assertNotNull(requisicao.getUltimaAlteracaoEstadoEm());
    }

    @Test
    void onCreate_quandoEstadoJaDefinido_devePreservarEstado() {
        RequisicaoManutencao requisicao = new RequisicaoManutencao();
        requisicao.setEstado(RequisicaoEstado.EM_PROGRESSO);

        requisicao.onCreate();

        assertEquals(RequisicaoEstado.EM_PROGRESSO, requisicao.getEstado());
        assertNotNull(requisicao.getCriadoEm());
        assertNotNull(requisicao.getUltimaAlteracaoEstadoEm());
    }
}