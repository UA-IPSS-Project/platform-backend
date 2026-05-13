package pt.florinhas.common_data.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ValenciaRepositoryTest {

    @Test
    void interface_DeveExistir() {

        assertNotNull(
                ValenciaRepository.class
        );

        assertTrue(
                ValenciaRepository.class
                        .isInterface()
        );
    }
}