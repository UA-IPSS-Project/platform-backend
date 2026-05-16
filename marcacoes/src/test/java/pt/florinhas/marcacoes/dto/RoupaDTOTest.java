package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RoupaDTOTest {

    @Test
    void deveCriarRoupaDTO() {

        RoupaDTO dto =
                new RoupaDTO(
                        1L,
                        "T-shirt",
                        2L,
                        "L",
                        3
                );

        assertEquals(1L, dto.getId());
        assertEquals("T-shirt", dto.getCategoria());
        assertEquals(2L, dto.getItemId());
        assertEquals("L", dto.getTamanho());
        assertEquals(3, dto.getQuantidade());
    }

    @Test
    void deveUsarSetters() {

        RoupaDTO dto =
                new RoupaDTO();

        dto.setId(10L);
        dto.setCategoria("Casaco");
        dto.setItemId(20L);
        dto.setTamanho("XL");
        dto.setQuantidade(5);

        assertEquals(10L, dto.getId());
        assertEquals("Casaco", dto.getCategoria());
        assertEquals(20L, dto.getItemId());
        assertEquals("XL", dto.getTamanho());
        assertEquals(5, dto.getQuantidade());
    }
}