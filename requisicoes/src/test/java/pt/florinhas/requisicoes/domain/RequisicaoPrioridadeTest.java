package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RequisicaoPrioridadeTest {

    @Test
    void values_DeveConterTodos() {

        assertEquals(
                4,
                RequisicaoPrioridade.values().length);

        assertEquals(
                RequisicaoPrioridade.BAIXA,
                RequisicaoPrioridade.valueOf("BAIXA"));

        assertEquals(
                RequisicaoPrioridade.URGENTE,
                RequisicaoPrioridade.valueOf("URGENTE"));
    }
}