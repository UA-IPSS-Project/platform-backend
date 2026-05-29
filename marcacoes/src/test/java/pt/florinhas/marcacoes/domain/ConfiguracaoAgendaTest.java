package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConfiguracaoAgendaTest {

    @Test
    void configuracaoAgenda_DeveGuardarValores() {

        ConfiguracaoAgenda config = new ConfiguracaoAgenda();

        config.setId(1L);
        config.setTipo("SECRETARIA");
        config.setCapacidadePorSlot(5);

        assertEquals(1L, config.getId());
        assertEquals("SECRETARIA", config.getTipo());
        assertEquals(5, config.getCapacidadePorSlot());
    }
}