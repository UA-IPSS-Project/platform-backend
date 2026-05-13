package pt.florinhas.marcacoes.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ItemArmazemRepositoryTest {

    @Autowired
    private ItemArmazemRepository repository;

    @Test
    @DisplayName("repository deve ser injetado")
    void repositoryDeveSerInjetado() {
        assertNotNull(repository);
    }
}