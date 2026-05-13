package pt.florinhas.marcacoes.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoupaRepositoryTest {

    @Test
    @DisplayName("RoupaRepository deve existir")
    void repositoryDeveExistir() {

        RoupaRepository repository =
                mock(RoupaRepository.class);

        assertNotNull(repository);
    }

    @Test
    @DisplayName("Classe RoupaRepository deve carregar")
    void classeDeveCarregar() {

        assertNotNull(RoupaRepository.class);
    }
}