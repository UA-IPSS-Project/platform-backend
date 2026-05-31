package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConfiguracaoSlotDTOTest {

    @Test
    void configuracaoSlotDTO_DeveGuardarValores() {

        ConfiguracaoSlotDTO dto = new ConfiguracaoSlotDTO();

        dto.setTipo("SECRETARIA");
        dto.setCapacidadePorSlot(5);

        assertEquals("SECRETARIA", dto.getTipo());
        assertEquals(5, dto.getCapacidadePorSlot());
    }
}