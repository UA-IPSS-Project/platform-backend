package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TipoManutencaoTest {

    @Test
    void settersAndGetters_DeveFuncionar() {

        TipoManutencao tipo =
                new TipoManutencao();

        tipo.setId(1L);
        tipo.setNome("Eletricidade");
        tipo.setDescricao("Descricao");

        assertEquals(
                1L,
                tipo.getId());

        assertEquals(
                "Eletricidade",
                tipo.getNome());

        assertEquals(
                "Descricao",
                tipo.getDescricao());
    }

    @Test
    void constructorVazio_DeveInicializarNull() {

        TipoManutencao tipo =
                new TipoManutencao();

        assertNull(tipo.getId());
        assertNull(tipo.getNome());
        assertNull(tipo.getDescricao());
    }
}