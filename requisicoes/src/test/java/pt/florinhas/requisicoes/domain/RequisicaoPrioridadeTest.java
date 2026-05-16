package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequisicaoPrioridadeTest {

    @Test
    void enum_DeveConterValoresEsperados() {

        assertEquals(
                RequisicaoPrioridade.BAIXA,
                RequisicaoPrioridade.valueOf(
                        "BAIXA"));

        assertEquals(
                RequisicaoPrioridade.MEDIA,
                RequisicaoPrioridade.valueOf(
                        "MEDIA"));

        assertEquals(
                RequisicaoPrioridade.ALTA,
                RequisicaoPrioridade.valueOf(
                        "ALTA"));

        assertEquals(
                RequisicaoPrioridade.URGENTE,
                RequisicaoPrioridade.valueOf(
                        "URGENTE"));
    }
}