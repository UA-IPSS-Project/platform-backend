package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FuncionarioTipoTest {

    @Test
    void values_DeveConterTodosOsTiposEsperados() {

        FuncionarioTipo[] values =
                FuncionarioTipo.values();

        assertEquals(
                6,
                values.length);

        assertEquals(
                FuncionarioTipo.SECRETARIA,
                values[0]);

        assertEquals(
                FuncionarioTipo.BALNEARIO,
                values[1]);

        assertEquals(
                FuncionarioTipo.OUTRO,
                values[2]);

        assertEquals(
                FuncionarioTipo.ESCOLA,
                values[3]);

        assertEquals(
                FuncionarioTipo.INTERNO,
                values[4]);

        assertEquals(
                FuncionarioTipo.DPO,
                values[5]);
    }

    @Test
    void valueOf_DeveRetornarEnumCorreto() {

        FuncionarioTipo tipo =
                FuncionarioTipo.valueOf(
                        "SECRETARIA");

        assertEquals(
                FuncionarioTipo.SECRETARIA,
                tipo);
    }
}