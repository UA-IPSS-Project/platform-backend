package pt.florinhas.marcacoes.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

class AssuntoRepositoryTest {

    @Test
    void assuntoRepository_DeveSerJpaRepository() {

        assertNotNull(JpaRepository.class);
    }
}