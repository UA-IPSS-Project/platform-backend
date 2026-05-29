package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.domain.BloqueioAgenda;

class BloqueioAgendaDTOTest {

    @Test
    void from_DeveConverter() {

        BloqueioAgenda bloqueio = new BloqueioAgenda();

        bloqueio.setId(1L);
        bloqueio.setData(LocalDate.now());
        bloqueio.setHoraInicio(LocalTime.of(10, 0));
        bloqueio.setHoraFim(LocalTime.of(11, 0));
        bloqueio.setMotivo("Teste");
        bloqueio.setTipo("SECRETARIA");

        BloqueioAgendaDTO dto =
                BloqueioAgendaDTO.from(bloqueio);

        assertEquals(1L, dto.id());
        assertEquals("Teste", dto.motivo());
        assertEquals("SECRETARIA", dto.tipo());
    }
}