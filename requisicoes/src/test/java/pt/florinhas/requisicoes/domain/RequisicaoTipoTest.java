package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RequisicaoTipoTest {

    @Test
    void values_DeveConterTodos() {

        assertEquals(
                3,
                RequisicaoTipo.values().length);

        assertEquals(
                RequisicaoTipo.MATERIAL,
                RequisicaoTipo.valueOf("MATERIAL"));

        assertEquals(
                RequisicaoTipo.MANUTENCAO,
                RequisicaoTipo.valueOf("MANUTENCAO"));
    }
}