package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CriarTipoManutencaoCatalogoRequestTest {

    @Test
    void constructorAndAccessors_DeveFuncionar() {

        CriarTipoManutencaoCatalogoRequest request =
                new CriarTipoManutencaoCatalogoRequest(
                        "Eletricidade",
                        "Descricao");

        assertEquals(
                "Eletricidade",
                request.nome());

        assertEquals(
                "Descricao",
                request.descricao());
    }
}