package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RoupaDTOTest {

    @Test
    void roupaDTO_DeveGuardarValores() {

        RoupaDTO dto = new RoupaDTO();

        dto.setId(1L);
        dto.setCategoria("CALÇAS");
        dto.setItemId(2L);
        dto.setTamanho("L");
        dto.setQuantidade(3);

        assertEquals(1L, dto.getId());
        assertEquals("CALÇAS", dto.getCategoria());
        assertEquals(2L, dto.getItemId());
        assertEquals("L", dto.getTamanho());
        assertEquals(3, dto.getQuantidade());
    }
}