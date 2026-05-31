package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MaterialCategoriaTest {

    @Test
    void values_DeveConterTodos() {

        assertEquals(
                5,
                MaterialCategoria.values().length);

        assertEquals(
                MaterialCategoria.ESCRITA,
                MaterialCategoria.valueOf("ESCRITA"));

        assertEquals(
                MaterialCategoria.OUTROS,
                MaterialCategoria.valueOf("OUTROS"));
    }
}