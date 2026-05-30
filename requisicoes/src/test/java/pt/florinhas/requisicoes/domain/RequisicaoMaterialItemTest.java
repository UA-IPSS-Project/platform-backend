package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class RequisicaoMaterialItemTest {

    @Test
    void settersAndGetters_DeveFuncionar() {

        RequisicaoMaterialItem item =
                new RequisicaoMaterialItem();

        Material material =
                new Material();

        RequisicaoMaterial requisicao =
                new RequisicaoMaterial();

        item.setId(1L);
        item.setMaterial(material);
        item.setQuantidade(5);
        item.setRequisicao(requisicao);

        assertEquals(
                1L,
                item.getId());

        assertEquals(
                material,
                item.getMaterial());

        assertEquals(
                5,
                item.getQuantidade());

        assertEquals(
                requisicao,
                item.getRequisicao());
    }

    @Test
    void constructorVazio_DeveInicializarNull() {

        RequisicaoMaterialItem item =
                new RequisicaoMaterialItem();

        assertNull(item.getId());
        assertNull(item.getMaterial());
        assertNull(item.getQuantidade());
        assertNull(item.getRequisicao());
    }
}