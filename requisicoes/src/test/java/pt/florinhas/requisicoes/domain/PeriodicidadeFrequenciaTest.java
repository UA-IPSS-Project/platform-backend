package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PeriodicidadeFrequenciaTest {

    @Test
    void values_DeveConterTodos() {

        assertEquals(
                3,
                PeriodicidadeFrequencia.values().length);

        assertEquals(
                PeriodicidadeFrequencia.DIARIA,
                PeriodicidadeFrequencia.valueOf("DIARIA"));

        assertEquals(
                PeriodicidadeFrequencia.MENSAL,
                PeriodicidadeFrequencia.valueOf("MENSAL"));
    }
}