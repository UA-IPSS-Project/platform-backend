package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequisicaoManutencaoTest {

    @Test
    void itens_DeveInicializarLista() {

        RequisicaoManutencao requisicao =
                new RequisicaoManutencao();

        assertNotNull(requisicao.getItens());

        assertTrue(requisicao.getItens().isEmpty());
    }
}