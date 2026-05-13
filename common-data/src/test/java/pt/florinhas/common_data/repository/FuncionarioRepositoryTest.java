package pt.florinhas.common_data.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FuncionarioRepositoryTest {

    @Test
    void interface_DeveExistir() {

        assertNotNull(
                FuncionarioRepository.class
        );

        assertTrue(
                FuncionarioRepository.class
                        .isInterface()
        );
    }
}