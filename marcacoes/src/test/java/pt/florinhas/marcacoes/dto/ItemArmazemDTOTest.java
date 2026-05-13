package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemArmazemDTOTest {

    @Test
    @DisplayName("deve criar DTO corretamente usando getters e setters")
    void deveCriarDTO() {

        ItemArmazemDTO dto = new ItemArmazemDTO();

        dto.setId(1L);
        dto.setCategoria("HIGIENE");
        dto.setNome("Champô");
        dto.setQuantidade(10);
        dto.setQuantidadeMinima(5);
        dto.setUnidade("un");
        dto.setMarca("Marca X");
        dto.setTamanho("M");
        dto.setVolume(1.5);
        dto.setDescricao("Descrição");
        dto.setEstado("OK");

        assertEquals(1L, dto.getId());
        assertEquals("HIGIENE", dto.getCategoria());
        assertEquals("Champô", dto.getNome());
        assertEquals(10, dto.getQuantidade());
        assertEquals(5, dto.getQuantidadeMinima());
        assertEquals("un", dto.getUnidade());
        assertEquals("Marca X", dto.getMarca());
        assertEquals("M", dto.getTamanho());
        assertEquals(1.5, dto.getVolume());
        assertEquals("Descrição", dto.getDescricao());
        assertEquals("OK", dto.getEstado());
    }

    @Test
    @DisplayName("deve criar DTO usando construtor vazio")
    void deveCriarDTOComConstrutorVazio() {

        ItemArmazemDTO dto = new ItemArmazemDTO();

        assertNotNull(dto);
    }

    @Test
    @DisplayName("deve criar DTO usando all args constructor")
    void deveCriarDTOComAllArgs() {

        ItemArmazemDTO dto = new ItemArmazemDTO(
                1L,
                "HIGIENE",
                "Champô",
                10,
                5,
                "un",
                "Marca",
                "M",
                1.0,
                "Descrição",
                "OK"
        );

        assertNotNull(dto);

        assertEquals(1L, dto.getId());
        assertEquals("HIGIENE", dto.getCategoria());
        assertEquals("Champô", dto.getNome());
        assertEquals(10, dto.getQuantidade());
        assertEquals(5, dto.getQuantidadeMinima());
        assertEquals("un", dto.getUnidade());
        assertEquals("Marca", dto.getMarca());
        assertEquals("M", dto.getTamanho());
        assertEquals(1.0, dto.getVolume());
        assertEquals("Descrição", dto.getDescricao());
        assertEquals("OK", dto.getEstado());
    }

    @Test
    @DisplayName("equals e hashCode devem funcionar")
    void equalsEHashCode_devemFuncionar() {

        ItemArmazemDTO dto1 = new ItemArmazemDTO();
        dto1.setId(1L);

        ItemArmazemDTO dto2 = new ItemArmazemDTO();
        dto2.setId(1L);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("toString não deve retornar null")
    void toString_naoDeveRetornarNull() {

        ItemArmazemDTO dto = new ItemArmazemDTO();
        dto.setNome("Champô");

        assertNotNull(dto.toString());
    }
}