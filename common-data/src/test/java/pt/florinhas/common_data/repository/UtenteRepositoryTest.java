package pt.florinhas.common_data.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class UtenteRepositoryTest {

    @Test
    void repository_DeveExistir() {

        Class<UtenteRepository> clazz =
                UtenteRepository.class;

        assertNotNull(clazz);
    }
}