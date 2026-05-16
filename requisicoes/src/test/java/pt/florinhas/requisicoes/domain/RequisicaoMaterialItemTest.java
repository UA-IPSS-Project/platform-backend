package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequisicaoMaterialItemTest {

    @Test
    void gettersAndSetters_DeveFuncionar() {

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

        assertEquals(1L, item.getId());
        assertEquals(material, item.getMaterial());
        assertEquals(5, item.getQuantidade());
        assertEquals(requisicao, item.getRequisicao());
    }
}