package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequisicaoTransporteItemTest {

    @Test
    void gettersAndSetters_DeveFuncionar() {

        RequisicaoTransporteItem item =
                new RequisicaoTransporteItem();

        Transporte transporte =
                new Transporte();

        RequisicaoTransporte requisicao =
                new RequisicaoTransporte();

        item.setId(1L);

        item.setTransporte(transporte);

        item.setRequisicao(requisicao);

        assertEquals(
                1L,
                item.getId());

        assertEquals(
                transporte,
                item.getTransporte());

        assertEquals(
                requisicao,
                item.getRequisicao());
    }
}