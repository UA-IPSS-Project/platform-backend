package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UtenteTest {

    @Test
    void isActivo_DeveGuardarEstado() {

        Utente utente =
                new Utente();

        utente.setActivo(true);

        assertTrue(
                utente.isActivo());
    }
}