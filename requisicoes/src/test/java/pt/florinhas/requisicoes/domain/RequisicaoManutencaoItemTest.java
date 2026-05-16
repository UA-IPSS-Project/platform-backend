package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequisicaoManutencaoItemTest {

    @Test
    void gettersAndSetters_DeveFuncionar() {

        RequisicaoManutencaoItem item =
                new RequisicaoManutencaoItem();

        ManutencaoItem manutencaoItem =
                new ManutencaoItem();

        RequisicaoManutencao requisicao =
                new RequisicaoManutencao();

        item.setId(1L);
        item.setManutencaoItem(manutencaoItem);
        item.setRequisicao(requisicao);
        item.setObservacoes("Teste");

        assertEquals(1L, item.getId());
        assertEquals(
                manutencaoItem,
                item.getManutencaoItem());

        assertEquals(
                requisicao,
                item.getRequisicao());

        assertEquals(
                "Teste",
                item.getObservacoes());
    }
}