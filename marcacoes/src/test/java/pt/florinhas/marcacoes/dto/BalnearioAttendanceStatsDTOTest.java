package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class BalnearioAttendanceStatsDTOTest {

    @Test
    void balnearioAttendanceStatsDTO_DeveGuardarValores() {

        BalnearioAttendanceStatsDTO dto = new BalnearioAttendanceStatsDTO();

        dto.setPeriodo("MES");
        dto.setTotalPresencas(10);
        dto.setTotalMarcacoes(20);
        dto.setTotalFaltas(2);
        dto.setTotalAgendadas(5);

        dto.setPresencasPorDia(
                List.of(
                        new BalnearioAttendanceStatsDTO.AttendanceData(
                                "2026-01-01",
                                3)));

        dto.setPresencasPorHora(Map.of(10, 5L));

        assertEquals("MES", dto.getPeriodo());
        assertEquals(10, dto.getTotalPresencas());
        assertEquals(20, dto.getTotalMarcacoes());
        assertEquals(2, dto.getTotalFaltas());
        assertEquals(5, dto.getTotalAgendadas());
    }
}