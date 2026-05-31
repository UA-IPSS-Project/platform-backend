package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ConsumoEstatisticaDTOTest {

    @Test
    void consumoEstatisticaDTO_DeveGuardarValores() {

        ConsumoEstatisticaDTO dto = new ConsumoEstatisticaDTO();

        dto.setPeriodo("MES");
        dto.setItens(List.of(
                new ConsumoEstatisticaDTO.ConsumoItemDTO("HIGIENE","Champô",5,"2026-01-01")));

        dto.setTotaisPorCategoria(Map.of("HIGIENE", 5));

        dto.setTotalGeral(5);

        assertEquals("MES", dto.getPeriodo());
        assertEquals(1, dto.getItens().size());
        assertEquals(5, dto.getTotalGeral());
    }
}