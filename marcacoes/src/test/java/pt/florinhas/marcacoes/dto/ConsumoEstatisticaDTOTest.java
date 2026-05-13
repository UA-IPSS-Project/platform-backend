package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsumoEstatisticaDTOTest {

    @Test
    @DisplayName("deve criar estatística corretamente")
    void deveCriarEstatistica() {

        ConsumoEstatisticaDTO.ConsumoItemDTO item =
                new ConsumoEstatisticaDTO.ConsumoItemDTO(
                        "HIGIENE",
                        "Champô",
                        2,
                        "2026-01-01"
                );

        ConsumoEstatisticaDTO dto = new ConsumoEstatisticaDTO();

        dto.setPeriodo("MES");
        dto.setItens(List.of(item));
        dto.setTotaisPorCategoria(Map.of("HIGIENE", 2));
        dto.setTotalGeral(2);

        assertEquals("MES", dto.getPeriodo());
        assertEquals(1, dto.getItens().size());
        assertEquals(2, dto.getTotalGeral());
        assertEquals(2, dto.getTotaisPorCategoria().get("HIGIENE"));
    }

    @Test
    @DisplayName("deve criar DTO usando construtor vazio")
    void deveCriarDTOComConstrutorVazio() {

        ConsumoEstatisticaDTO dto =
                new ConsumoEstatisticaDTO();

        assertNotNull(dto);
    }

    @Test
    @DisplayName("deve criar DTO usando all args constructor")
    void deveCriarDTOComAllArgs() {

        ConsumoEstatisticaDTO dto =
                new ConsumoEstatisticaDTO(
                        "MES",
                        List.of(),
                        Map.of(),
                        0
                );

        assertNotNull(dto);

        assertEquals("MES", dto.getPeriodo());
        assertEquals(0, dto.getTotalGeral());
    }

    @Test
    @DisplayName("deve criar ConsumoItemDTO corretamente")
    void deveCriarConsumoItemDTO() {

        ConsumoEstatisticaDTO.ConsumoItemDTO dto =
                new ConsumoEstatisticaDTO.ConsumoItemDTO();

        dto.setCategoria("HIGIENE");
        dto.setNome("Champô");
        dto.setQuantidade(5);
        dto.setData("2026-01-01");

        assertEquals("HIGIENE", dto.getCategoria());
        assertEquals("Champô", dto.getNome());
        assertEquals(5, dto.getQuantidade());
        assertEquals("2026-01-01", dto.getData());
    }

    @Test
    @DisplayName("ConsumoItemDTO all args deve funcionar")
    void consumoItemDTOAllArgs_deveFuncionar() {

        ConsumoEstatisticaDTO.ConsumoItemDTO dto =
                new ConsumoEstatisticaDTO.ConsumoItemDTO(
                        "HIGIENE",
                        "Champô",
                        3,
                        "2026-01-01"
                );

        assertEquals("HIGIENE", dto.getCategoria());
        assertEquals("Champô", dto.getNome());
        assertEquals(3, dto.getQuantidade());
        assertEquals("2026-01-01", dto.getData());
    }

    @Test
    @DisplayName("equals e hashCode devem funcionar")
    void equalsEHashCode_devemFuncionar() {

        ConsumoEstatisticaDTO dto1 =
                new ConsumoEstatisticaDTO();

        dto1.setPeriodo("MES");

        ConsumoEstatisticaDTO dto2 =
                new ConsumoEstatisticaDTO();

        dto2.setPeriodo("MES");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("toString não deve retornar null")
    void toString_naoDeveRetornarNull() {

        ConsumoEstatisticaDTO dto =
                new ConsumoEstatisticaDTO();

        dto.setPeriodo("MES");

        assertNotNull(dto.toString());
    }
}