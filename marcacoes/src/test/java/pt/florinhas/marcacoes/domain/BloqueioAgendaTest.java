package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class BloqueioAgendaTest {

    @Test
    void builder_DeveCriarObjeto() {

        BloqueioAgenda bloqueio = BloqueioAgenda.builder()
                .id(1L)
                .data(LocalDate.now())
                .horaInicio(LocalTime.of(10, 0))
                .horaFim(LocalTime.of(11, 0))
                .motivo("Teste")
                .build();

        assertEquals(1L, bloqueio.getId());
        assertEquals("Teste", bloqueio.getMotivo());
        assertEquals("SECRETARIA", bloqueio.getTipo());
    }
}