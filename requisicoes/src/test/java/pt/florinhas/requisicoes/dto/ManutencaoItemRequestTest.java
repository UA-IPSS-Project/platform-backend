package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ManutencaoItemRequestTest {

    @Test
    void constructorAndAccessors_DeveFuncionar() {

        ManutencaoItemRequest request =
                new ManutencaoItemRequest(
                        1L,
                        2L,
                        "Obs");

        assertEquals(
                1L,
                request.itemId());

        assertEquals(
                2L,
                request.transporteId());

        assertEquals(
                "Obs",
                request.observacoes());
    }
}