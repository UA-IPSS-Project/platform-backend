package pt.florinhas.common_data.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UtilizadorRepositoryTest {

    @Test
    void interface_DeveExistir() {

        assertNotNull(
                UtilizadorRepository.class
        );

        assertTrue(
                UtilizadorRepository.class
                        .isInterface()
        );
    }
}