package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AtualizarConfiguracaoSlotRequestTest {

    @Test
    void deveCriarRequest() {

        AtualizarConfiguracaoSlotRequest request =
                new AtualizarConfiguracaoSlotRequest(5);

        assertEquals(
                5,
                request.getCapacidadePorSlot()
        );
    }

    @Test
    void deveAtualizarCapacidade() {

        AtualizarConfiguracaoSlotRequest request =
                new AtualizarConfiguracaoSlotRequest();

        request.setCapacidadePorSlot(10);

        assertEquals(
                10,
                request.getCapacidadePorSlot()
        );
    }
}