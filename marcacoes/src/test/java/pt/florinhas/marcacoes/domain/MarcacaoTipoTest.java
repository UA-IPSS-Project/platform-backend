package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MarcacaoTipoTest {

    @Test
    void values_DeveConterTipos() {

        assertEquals(2, MarcacaoTipo.values().length);
    }

    @Test
    void valueOf_DeveRetornarTipo() {

        assertEquals(
                MarcacaoTipo.SECRETARIA,
                MarcacaoTipo.valueOf("SECRETARIA"));
    }
}