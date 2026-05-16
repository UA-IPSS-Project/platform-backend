package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AssuntoTest {

    @Test
    void deveCriarAssuntoComConstrutorCompleto() {

        Assunto assunto =
                new Assunto(
                        1L,
                        "Documentos",
                        true
                );

        assertEquals(1L, assunto.getId());
        assertEquals("Documentos", assunto.getNome());
        assertTrue(assunto.isAtivo());
    }

    @Test
    void deveCriarAssuntoComConstrutorSimples() {

        Assunto assunto =
                new Assunto("Consulta");

        assertEquals("Consulta", assunto.getNome());
        assertTrue(assunto.isAtivo());
    }
}