package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class BloqueioAgendaTest {

    @Test
    void deveCriarBloqueioAgenda() {

        LocalDate data = LocalDate.now();
        LocalTime inicio = LocalTime.of(10, 0);
        LocalTime fim = LocalTime.of(11, 0);

        BloqueioAgenda bloqueio = BloqueioAgenda.builder()
                .id(1L)
                .data(data)
                .horaInicio(inicio)
                .horaFim(fim)
                .motivo("Reunião")
                .tipo("SECRETARIA")
                .build();

        assertEquals(1L, bloqueio.getId());
        assertEquals(data, bloqueio.getData());
        assertEquals(inicio, bloqueio.getHoraInicio());
        assertEquals(fim, bloqueio.getHoraFim());
        assertEquals("Reunião", bloqueio.getMotivo());
        assertEquals("SECRETARIA", bloqueio.getTipo());
    }
}