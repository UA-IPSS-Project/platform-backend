package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class RequisicaoTransporteTest {

    @Test
    void constructor_DeveInicializarLista() {

        RequisicaoTransporte requisicao =
                new RequisicaoTransporte();

        assertNotNull(
                requisicao.getTransportes());

        assertEquals(
                List.of(),
                requisicao.getTransportes());
    }

    @Test
    void settersAndGetters_DeveFuncionar() {

        RequisicaoTransporte requisicao =
                new RequisicaoTransporte();

        Transporte transporte =
                new Transporte();

        RequisicaoTransporteItem item =
                new RequisicaoTransporteItem();

        LocalDateTime saida =
                LocalDateTime.now();

        LocalDateTime regresso =
                saida.plusHours(2);

        requisicao.setDestino("Aveiro");
        requisicao.setDataHoraSaida(saida);
        requisicao.setDataHoraRegresso(regresso);
        requisicao.setNumeroPassageiros(5);
        requisicao.setCondutor("Nuno");
        requisicao.setTransporte(transporte);
        requisicao.setTransportes(
                List.of(item));

        assertEquals(
                "Aveiro",
                requisicao.getDestino());

        assertEquals(
                saida,
                requisicao.getDataHoraSaida());

        assertEquals(
                regresso,
                requisicao.getDataHoraRegresso());

        assertEquals(
                5,
                requisicao.getNumeroPassageiros());

        assertEquals(
                "Nuno",
                requisicao.getCondutor());

        assertEquals(
                transporte,
                requisicao.getTransporte());

        assertEquals(
                1,
                requisicao.getTransportes().size());
    }
}