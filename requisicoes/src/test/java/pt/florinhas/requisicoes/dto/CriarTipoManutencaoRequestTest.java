package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CriarTipoManutencaoRequestTest {

    @Test
    void constructorAndAccessors_DeveFuncionar() {

        CriarTipoManutencaoRequest request =
                new CriarTipoManutencaoRequest(
                        "Canalizacao",
                        "Descricao");

        assertEquals(
                "Canalizacao",
                request.nome());

        assertEquals(
                "Descricao",
                request.descricao());
    }
}