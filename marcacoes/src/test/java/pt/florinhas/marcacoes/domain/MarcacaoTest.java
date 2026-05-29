package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MarcacaoTest {

    @Test
    void onCreate_DeveDefinirCriadoEm() {

        Marcacao marcacao = new Marcacao();

        marcacao.onCreate();

        assertNotNull(marcacao.getCriadoEm());
    }
}