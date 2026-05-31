package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EventoEstadoTest {

    @Test
    void values_DeveConterEstados() {

        assertEquals(8, EventoEstado.values().length);
    }

    @Test
    void valueOf_DeveRetornarEstado() {

        assertEquals(
                EventoEstado.AGENDADO,
                EventoEstado.valueOf("AGENDADO"));
    }
}