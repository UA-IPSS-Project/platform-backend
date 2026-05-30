package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CriarMaterialRequestTest {

    @Test
    void constructorAndAccessors_DeveFuncionar() {

        CriarMaterialRequest request =
                new CriarMaterialRequest(
                        "Caneta",
                        "ESCRITA",
                        "Cor",
                        "Azul");

        assertEquals(
                "Caneta",
                request.nome());

        assertEquals(
                "ESCRITA",
                request.categoria());

        assertEquals(
                "Cor",
                request.atributo());

        assertEquals(
                "Azul",
                request.valorAtributo());
    }
}