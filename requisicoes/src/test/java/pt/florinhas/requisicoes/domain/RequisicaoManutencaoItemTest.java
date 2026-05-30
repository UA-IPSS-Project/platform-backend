package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class RequisicaoManutencaoItemTest {

    @Test
    void settersAndGetters_DeveFuncionar() {

        RequisicaoManutencaoItem item =
                new RequisicaoManutencaoItem();

        ManutencaoItem manutencaoItem =
                new ManutencaoItem();

        RequisicaoManutencao requisicao =
                new RequisicaoManutencao();

        Transporte transporte =
                new Transporte();

        item.setId(1L);
        item.setManutencaoItem(manutencaoItem);
        item.setRequisicao(requisicao);
        item.setTransporte(transporte);
        item.setObservacoes("Obs");

        assertEquals(
                1L,
                item.getId());

        assertEquals(
                manutencaoItem,
                item.getManutencaoItem());

        assertEquals(
                requisicao,
                item.getRequisicao());

        assertEquals(
                transporte,
                item.getTransporte());

        assertEquals(
                "Obs",
                item.getObservacoes());
    }

    @Test
    void constructorVazio_DeveInicializarNull() {

        RequisicaoManutencaoItem item =
                new RequisicaoManutencaoItem();

        assertNull(item.getId());
        assertNull(item.getManutencaoItem());
        assertNull(item.getRequisicao());
        assertNull(item.getTransporte());
        assertNull(item.getObservacoes());
    }
}