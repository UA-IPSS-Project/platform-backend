package pt.florinhas.common_data.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class FuncionarioRepositoryTest {

    @Test
    void repository_DeveExistir() {

        Class<FuncionarioRepository> clazz =
                FuncionarioRepository.class;

        assertNotNull(clazz);
    }
}