package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class RequisicaoManutencaoTest {

    @Test
    void constructor_DeveInicializarLista() {

        RequisicaoManutencao requisicao =
                new RequisicaoManutencao();

        assertNotNull(
                requisicao.getItens());

        assertEquals(
                List.of(),
                requisicao.getItens());
    }

    @Test
    void settersAndGetters_DeveFuncionar() {

        RequisicaoManutencao requisicao =
                new RequisicaoManutencao();

        RequisicaoManutencaoItem item =
                new RequisicaoManutencaoItem();

        requisicao.setItens(
                List.of(item));

        assertEquals(
                1,
                requisicao.getItens().size());

        assertEquals(
                item,
                requisicao.getItens().get(0));
    }
}