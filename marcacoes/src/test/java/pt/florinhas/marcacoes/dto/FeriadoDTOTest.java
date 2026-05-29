package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FeriadoDTOTest {

    @Test
    void feriadoDTO_DeveGuardarValores() {

        FeriadoDTO dto = new FeriadoDTO();

        dto.setDate("2026-12-25");
        dto.setLocalName("Natal");

        assertEquals("2026-12-25", dto.getDate());
        assertEquals("Natal", dto.getLocalName());
    }
}