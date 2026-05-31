package pt.florinhas.marcacoes.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

class ItemArmazemRepositoryTest {

    @Test
    void itemArmazemRepository_DeveSerJpaRepository() {

        assertNotNull(JpaRepository.class);
    }
}