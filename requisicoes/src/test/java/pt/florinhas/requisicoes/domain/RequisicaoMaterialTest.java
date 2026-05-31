package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class RequisicaoMaterialTest {

    @Test
    void constructor_DeveInicializarLista() {

        RequisicaoMaterial requisicao =
                new RequisicaoMaterial();

        assertNotNull(
                requisicao.getItens());

        assertEquals(
                List.of(),
                requisicao.getItens());
    }

    @Test
    void settersAndGetters_DeveFuncionar() {

        RequisicaoMaterial requisicao =
                new RequisicaoMaterial();

        RequisicaoMaterialItem item =
                new RequisicaoMaterialItem();

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