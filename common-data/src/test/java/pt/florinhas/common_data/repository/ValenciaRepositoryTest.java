package pt.florinhas.common_data.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ValenciaRepositoryTest {

    @Test
    void repository_DeveExistir() {

        Class<ValenciaRepository> clazz =
                ValenciaRepository.class;

        assertNotNull(clazz);
    }
}