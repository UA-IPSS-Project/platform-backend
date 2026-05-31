package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AtualizarConfiguracaoSlotRequestTest {

    @Test
    void atualizarConfiguracaoSlotRequest_DeveGuardarValor() {

        AtualizarConfiguracaoSlotRequest request = new AtualizarConfiguracaoSlotRequest();

        request.setCapacidadePorSlot(5);

        assertEquals(5, request.getCapacidadePorSlot());
    }
}