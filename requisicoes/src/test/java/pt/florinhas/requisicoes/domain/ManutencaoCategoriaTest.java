package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ManutencaoCategoriaTest {

    @Test
    void enum_DeveConterValoresEsperados() {

        assertEquals(
                ManutencaoCategoria.CATL,
                ManutencaoCategoria.valueOf("CATL"));

        assertEquals(
                ManutencaoCategoria.VEICULOS,
                ManutencaoCategoria.valueOf("VEICULOS"));
    }
}