package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FuncionarioTipoTest {

    @Test
    void values_DeveConterTodosOsTipos() {

        assertEquals(
                6,
                FuncionarioTipo.values().length);
    }

    @Test
    void valueOf_DeveRetornarEnum() {

        assertEquals(
                FuncionarioTipo.SECRETARIA,
                FuncionarioTipo.valueOf("SECRETARIA"));
    }
}