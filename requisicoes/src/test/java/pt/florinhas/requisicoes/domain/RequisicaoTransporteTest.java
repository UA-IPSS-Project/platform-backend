package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class RequisicaoTransporteTest {

    @Test
    void gettersAndSetters_DeveFuncionar() {

        RequisicaoTransporte requisicao =
                new RequisicaoTransporte();

        Transporte transporte =
                new Transporte();

        LocalDateTime saida =
                LocalDateTime.now();

        LocalDateTime regresso =
                saida.plusHours(2);

        requisicao.setId(1L);

        requisicao.setDestino("Aveiro");

        requisicao.setDataHoraSaida(saida);

        requisicao.setDataHoraRegresso(
                regresso);

        requisicao.setNumeroPassageiros(5);

        requisicao.setCondutor("João");

        requisicao.setTransporte(transporte);

        assertEquals(
                1L,
                requisicao.getId());

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
                "João",
                requisicao.getCondutor());

        assertEquals(
                transporte,
                requisicao.getTransporte());
    }

    @Test
    void transportes_DeveInicializarLista() {

        RequisicaoTransporte requisicao =
                new RequisicaoTransporte();

        assertNotNull(
                requisicao.getTransportes());

        assertTrue(
                requisicao.getTransportes()
                        .isEmpty());
    }
}