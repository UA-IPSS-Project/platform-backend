package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class BalnearioAttendanceStatsDTOTest {

    @Test
    void deveCriarDTO() {

        BalnearioAttendanceStatsDTO.AttendanceData data =
                new BalnearioAttendanceStatsDTO.AttendanceData(
                        "2025-01-01",
                        5
                );

        BalnearioAttendanceStatsDTO dto =
                new BalnearioAttendanceStatsDTO(
                        "MES",
                        20,
                        30,
                        2,
                        5,
                        List.of(data),
                        Map.of(10, 4L)
                );

        assertEquals("MES", dto.getPeriodo());
        assertEquals(20, dto.getTotalPresencas());
        assertEquals(30, dto.getTotalMarcacoes());
        assertEquals(2, dto.getTotalFaltas());
        assertEquals(5, dto.getTotalAgendadas());
    }
}