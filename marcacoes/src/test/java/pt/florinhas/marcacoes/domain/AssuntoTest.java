package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AssuntoTest {

    @Test
    void constructor_DeveDefinirNomeEAtivo() {

        Assunto assunto = new Assunto("Teste");

        assertEquals("Teste", assunto.getNome());
        assertTrue(assunto.isAtivo());
    }

    @Test
    void allArgsConstructor_DeveGuardarValores() {

        Assunto assunto = new Assunto(1L, "Secretaria", false);

        assertEquals(1L, assunto.getId());
        assertEquals("Secretaria", assunto.getNome());
        assertEquals(false, assunto.isAtivo());
    }
}