package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ManutencaoCategoriaTest {

    @Test
    void values_DeveConterTodos() {

        assertEquals(
                5,
                ManutencaoCategoria.values().length);

        assertEquals(
                ManutencaoCategoria.CATL,
                ManutencaoCategoria.valueOf("CATL"));

        assertEquals(
                ManutencaoCategoria.VEICULOS,
                ManutencaoCategoria.valueOf("VEICULOS"));
    }
}