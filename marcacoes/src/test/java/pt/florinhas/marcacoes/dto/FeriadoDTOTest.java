package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FeriadoDTOTest {

    @Test
    void deveDefinirValores() {

        FeriadoDTO dto =
                new FeriadoDTO();

        dto.setDate("2025-12-25");
        dto.setLocalName("Natal");

        assertEquals("2025-12-25", dto.getDate());
        assertEquals("Natal", dto.getLocalName());
    }
}