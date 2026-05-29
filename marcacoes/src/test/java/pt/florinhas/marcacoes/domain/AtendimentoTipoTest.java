package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AtendimentoTipoTest {

    @Test
    void values_DeveConterTipos() {

        assertEquals(2, AtendimentoTipo.values().length);
    }

    @Test
    void valueOf_DeveRetornarEnum() {

        assertEquals(AtendimentoTipo.PRESENCIAL, AtendimentoTipo.valueOf("PRESENCIAL"));
    }
}