package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequisicaoMaterialTest {

    @Test
    void itens_DeveInicializarLista() {

        RequisicaoMaterial requisicao =
                new RequisicaoMaterial();

        assertNotNull(requisicao.getItens());

        assertTrue(requisicao.getItens().isEmpty());
    }
}