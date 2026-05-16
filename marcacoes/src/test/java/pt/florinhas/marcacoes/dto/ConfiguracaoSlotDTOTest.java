package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConfiguracaoSlotDTOTest {

    @Test
    void deveCriarDTO() {

        ConfiguracaoSlotDTO dto =
                new ConfiguracaoSlotDTO(
                        "SECRETARIA",
                        5
                );

        assertEquals("SECRETARIA", dto.getTipo());
        assertEquals(5, dto.getCapacidadePorSlot());
    }
}