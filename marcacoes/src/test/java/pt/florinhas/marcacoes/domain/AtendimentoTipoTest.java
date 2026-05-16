package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AtendimentoTipoTest {

    @Test
    void deveConterPresencial() {
        assertEquals("PRESENCIAL", AtendimentoTipo.PRESENCIAL.name());
    }

    @Test
    void deveConterRemoto() {
        assertEquals("REMOTO", AtendimentoTipo.REMOTO.name());
    }
}