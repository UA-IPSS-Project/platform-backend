package pt.florinhas.marcacoes.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for aggregated attendance statistics in the Balneário service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalnearioAttendanceStatsDTO {
    /**
     * Period: DIA, SEMANA, MES
     */
    private String periodo;
    
    /**
     * Total attendances in the period.
     */
    private long totalPresencas;
    
    /**
     * Total number of appointments (all states except maybe cancelled if the logic excludes it).
     */
    private long totalMarcacoes;

    /**
     * Attendances with status FALTOU.
     */
    private long totalFaltou;

    /**
     * Attendances grouped by date (for time series charts).
     */
    private List<AttendanceData> presencasPorDia;
    
    /**
     * Peak hours: hour (0-23) -> attendance count.
     */
    private Map<Integer, Long> presencasPorHora;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceData {
        private String data;
        private long quantidade;
    }
}
