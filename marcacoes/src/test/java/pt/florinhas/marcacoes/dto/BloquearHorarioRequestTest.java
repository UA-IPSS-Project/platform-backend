package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class BloquearHorarioRequestTest {

    @Test
    void bloquearHorarioRequest_DeveGuardarValores() {

        BloquearHorarioRequest request = new BloquearHorarioRequest();

        request.setData(LocalDate.now());
        request.setHoraInicio(LocalTime.of(10, 0));
        request.setHoraFim(LocalTime.of(11, 0));
        request.setMotivo("Teste");
        request.setFuncionarioId(1L);
        request.setTipo("BALNEARIO");

        assertEquals("Teste", request.getMotivo());
        assertEquals(1L, request.getFuncionarioId());
        assertEquals("BALNEARIO", request.getTipo());
    }
}