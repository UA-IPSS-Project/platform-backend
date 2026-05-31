package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ManutencaoItemTest {

    @Test
    void constructorCompleto_DeveFuncionar() {

        ManutencaoItem item =
                new ManutencaoItem(
                        1L,
                        "CATL",
                        "Sala",
                        "Luz");

        assertEquals(
                1L,
                item.getId());

        assertEquals(
                "CATL",
                item.getCategoria());

        assertEquals(
                "Sala",
                item.getEspaco());

        assertEquals(
                "Luz",
                item.getItemVerificacao());
    }

    @Test
    void settersAndGetters_DeveFuncionar() {

        ManutencaoItem item =
                new ManutencaoItem();

        item.setId(2L);
        item.setCategoria("CRECHE");
        item.setEspaco("Corredor");
        item.setItemVerificacao("Porta");

        assertEquals(
                2L,
                item.getId());

        assertEquals(
                "CRECHE",
                item.getCategoria());

        assertEquals(
                "Corredor",
                item.getEspaco());

        assertEquals(
                "Porta",
                item.getItemVerificacao());
    }

    @Test
    void constructorVazio_DeveInicializarNull() {

        ManutencaoItem item =
                new ManutencaoItem();

        assertNull(item.getId());
        assertNull(item.getCategoria());
        assertNull(item.getEspaco());
        assertNull(item.getItemVerificacao());
    }
}