package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UtenteTest {

    @Test
    void activo_DeveSerDefinidoCorretamente() {

        Utente utente =
                new Utente();

        utente.setActivo(true);

        assertTrue(
                utente.isActivo());

        utente.setActivo(false);

        assertFalse(
                utente.isActivo());
    }
}