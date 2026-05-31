package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ItemArmazemDTOTest {

    @Test
    void itemArmazemDTO_DeveGuardarValores() {

        ItemArmazemDTO dto = new ItemArmazemDTO();

        dto.setId(1L);
        dto.setCategoria("HIGIENE");
        dto.setNome("Champô");
        dto.setQuantidade(10);
        dto.setQuantidadeMinima(2);
        dto.setUnidade("un");
        dto.setMarca("Marca");
        dto.setTamanho("L");
        dto.setVolume(1.5);
        dto.setDescricao("Teste");
        dto.setEstado("OK");

        assertEquals(1L, dto.getId());
        assertEquals("HIGIENE", dto.getCategoria());
        assertEquals("Champô", dto.getNome());
        assertEquals(10, dto.getQuantidade());
        assertEquals(2, dto.getQuantidadeMinima());
        assertEquals("un", dto.getUnidade());
        assertEquals("Marca", dto.getMarca());
        assertEquals("L", dto.getTamanho());
        assertEquals(1.5, dto.getVolume());
        assertEquals("Teste", dto.getDescricao());
        assertEquals("OK", dto.getEstado());
    }
}