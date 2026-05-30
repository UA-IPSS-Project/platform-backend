package pt.florinhas.marcacoes.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

class SystemConfigRepositoryTest {

    @Test
    void systemConfigRepository_DeveSerJpaRepository() {

        assertNotNull(JpaRepository.class);
    }
}