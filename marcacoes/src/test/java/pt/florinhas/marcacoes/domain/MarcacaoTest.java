package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MarcacaoTest {

    @Test
    @DisplayName("deve preencher criadoEm no onCreate")
    void onCreate_devePreencherCriadoEm() {
        Marcacao marcacao = new Marcacao();

        marcacao.onCreate();

        assertNotNull(marcacao.getCriadoEm());
    }
}