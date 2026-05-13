package pt.florinhas.common_data.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UtenteRepositoryTest {

    @Test
    void interface_DeveExistir() {

        assertNotNull(
                UtenteRepository.class
        );

        assertTrue(
                UtenteRepository.class
                        .isInterface()
        );
    }
}