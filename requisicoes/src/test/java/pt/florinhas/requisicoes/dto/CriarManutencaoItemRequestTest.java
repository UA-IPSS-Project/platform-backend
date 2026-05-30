package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CriarManutencaoItemRequestTest {

    @Test
    void constructorAndAccessors_DeveFuncionar() {

        CriarManutencaoItemRequest request =
                new CriarManutencaoItemRequest(
                        "CATL",
                        "Sala",
                        "Luzes");

        assertEquals(
                "CATL",
                request.categoria());

        assertEquals(
                "Sala",
                request.espaco());

        assertEquals(
                "Luzes",
                request.itemVerificacao());
    }
}