package pt.florinhas.marcacoes.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para dados agregados de estatísticas de consumo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumoEstatisticaDTO {

    /**
     * Período: DIA, SEMANA, MES
     */
    private String periodo;

    /**
     * Lista de consumos por item e data.
     */
    private List<ConsumoItemDTO> itens;

    /**
     * Totais de consumo agrupados por categoria.
     */
    private Map<String, Integer> totaisPorCategoria;

    /**
     * Total geral de consumos no período.
     */
    private int totalGeral;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumoItemDTO {
        private String categoria;
        private String nome;
        private int quantidade;
        private String data; // ISO date string for time series
    }
}