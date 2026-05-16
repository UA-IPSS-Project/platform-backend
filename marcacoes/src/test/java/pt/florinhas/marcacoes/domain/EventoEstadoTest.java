package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventoEstadoTest {

    @Test
    @DisplayName("deve conter estados esperados")
    void deveConterEstadosEsperados() {
        assertEquals("AGENDADO", EventoEstado.AGENDADO.name());
        assertEquals("EM_PROGRESSO", EventoEstado.EM_PROGRESSO.name());
        assertEquals("CONCLUIDO", EventoEstado.CONCLUIDO.name());
        assertEquals("CANCELADO", EventoEstado.CANCELADO.name());
        assertEquals("NAO_COMPARECIDO", EventoEstado.NAO_COMPARECIDO.name());
        assertEquals("EM_PREENCHIMENTO", EventoEstado.EM_PREENCHIMENTO.name());
        assertEquals("INVALIDO", EventoEstado.INVALIDO.name());
    }
}