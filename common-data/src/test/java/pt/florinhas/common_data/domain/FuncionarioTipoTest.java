package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FuncionarioTipoTest {

    @Test
    void values_DeveConterEnums() {

        assertNotNull(
                FuncionarioTipo.SECRETARIA
        );

        assertNotNull(
                FuncionarioTipo.BALNEARIO
        );

        assertNotNull(
                FuncionarioTipo.ESCOLA
        );

        assertNotNull(
                FuncionarioTipo.INTERNO
        );

        assertNotNull(
                FuncionarioTipo.DPO
        );
    }
}