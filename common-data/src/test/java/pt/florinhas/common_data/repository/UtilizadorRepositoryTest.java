package pt.florinhas.common_data.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class UtilizadorRepositoryTest {

    @Test
    void repository_DeveExistir() {

        Class<UtilizadorRepository> clazz =
                UtilizadorRepository.class;

        assertNotNull(clazz);
    }
}