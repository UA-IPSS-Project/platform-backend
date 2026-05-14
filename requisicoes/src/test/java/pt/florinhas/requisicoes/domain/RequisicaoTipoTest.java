package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequisicaoTipoTest {

    @Test
    void enum_DeveConterValoresEsperados() {

        assertEquals(
                RequisicaoTipo.MATERIAL,
                RequisicaoTipo.valueOf(
                        "MATERIAL"));

        assertEquals(
                RequisicaoTipo.TRANSPORTE,
                RequisicaoTipo.valueOf(
                        "TRANSPORTE"));

        assertEquals(
                RequisicaoTipo.MANUTENCAO,
                RequisicaoTipo.valueOf(
                        "MANUTENCAO"));
    }
}