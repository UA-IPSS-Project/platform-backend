package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class RequisicaoTransporteItemTest {

    @Test
    void settersAndGetters_DeveFuncionar() {

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

    @Test
    void constructorVazio_DeveInicializarNull() {

        RequisicaoTransporteItem item =
                new RequisicaoTransporteItem();

        assertNull(item.getId());
        assertNull(item.getTransporte());
        assertNull(item.getRequisicao());
    }
}