package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConfiguracaoAgendaTest {

    @Test
    void deveCriarConfiguracaoAgenda() {

        ConfiguracaoAgenda config =
                new ConfiguracaoAgenda(
                        1L,
                        "SECRETARIA",
                        5
                );

        assertEquals(1L, config.getId());
        assertEquals("SECRETARIA", config.getTipo());
        assertEquals(5, config.getCapacidadePorSlot());
    }
}