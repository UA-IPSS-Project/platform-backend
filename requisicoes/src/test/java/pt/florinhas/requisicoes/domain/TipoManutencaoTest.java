package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TipoManutencaoTest {

    @Test
    void gettersAndSetters_DeveFuncionar() {

        TipoManutencao tipo =
                new TipoManutencao();

        tipo.setId(1L);

        tipo.setNome("Elétrica");

        tipo.setDescricao("Teste");

        assertEquals(
                1L,
                tipo.getId());

        assertEquals(
                "Elétrica",
                tipo.getNome());

        assertEquals(
                "Teste",
                tipo.getDescricao());
    }
}