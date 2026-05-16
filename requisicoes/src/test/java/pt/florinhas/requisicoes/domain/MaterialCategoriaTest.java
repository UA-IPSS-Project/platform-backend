package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MaterialCategoriaTest {

    @Test
    void enum_DeveConterValoresEsperados() {

        assertEquals(
                MaterialCategoria.ESCRITA,
                MaterialCategoria.valueOf("ESCRITA"));

        assertEquals(
                MaterialCategoria.OUTROS,
                MaterialCategoria.valueOf("OUTROS"));
    }
}