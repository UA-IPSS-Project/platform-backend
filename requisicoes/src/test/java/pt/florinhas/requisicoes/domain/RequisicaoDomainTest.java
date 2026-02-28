package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class RequisicaoDomainTest {

    @Test
    void onCreate_quandoEstadoNulo_deveDefinirAbertaEDataCriacao() {
        RequisicaoManutencao requisicao = new RequisicaoManutencao();

        requisicao.onCreate();

        assertEquals(RequisicaoEstado.ABERTA, requisicao.getEstado());
        assertNotNull(requisicao.getCriadoEm());
    }

    @Test
    void onCreate_quandoEstadoJaDefinido_devePreservarEstado() {
        RequisicaoManutencao requisicao = new RequisicaoManutencao();
        requisicao.setEstado(RequisicaoEstado.EM_ANALISE);

        requisicao.onCreate();

        assertEquals(RequisicaoEstado.EM_ANALISE, requisicao.getEstado());
        assertNotNull(requisicao.getCriadoEm());
    }
}