package pt.florinhas.common_data.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UtenteTest {

    @Test
    void gettersAndSetters_DeveFuncionar() {

        Utente utente =
                new Utente();

        utente.setActivo(true);

        assertTrue(
                utente.isActivo()
        );
    }
}