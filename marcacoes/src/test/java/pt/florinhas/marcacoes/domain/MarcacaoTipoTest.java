package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MarcacaoTipoTest {

    @Test
    @DisplayName("deve conter tipos esperados")
    void deveConterTiposEsperados() {
        assertEquals("SECRETARIA", MarcacaoTipo.SECRETARIA.name());
        assertEquals("BALNEARIO", MarcacaoTipo.BALNEARIO.name());
    }
}