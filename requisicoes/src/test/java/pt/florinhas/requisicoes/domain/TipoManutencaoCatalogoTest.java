package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TipoManutencaoCatalogoTest {

    @Test
    void settersAndGetters_DeveFuncionar() {

        TipoManutencaoCatalogo tipo =
                new TipoManutencaoCatalogo();

        tipo.setId(1L);
        tipo.setNome("Canalizacao");
        tipo.setDescricao("Descricao");

        assertEquals(
                1L,
                tipo.getId());

        assertEquals(
                "Canalizacao",
                tipo.getNome());

        assertEquals(
                "Descricao",
                tipo.getDescricao());
    }

    @Test
    void constructorVazio_DeveInicializarNull() {

        TipoManutencaoCatalogo tipo =
                new TipoManutencaoCatalogo();

        assertNull(tipo.getId());
        assertNull(tipo.getNome());
        assertNull(tipo.getDescricao());
    }
}