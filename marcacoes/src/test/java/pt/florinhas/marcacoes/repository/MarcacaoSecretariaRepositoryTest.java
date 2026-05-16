package pt.florinhas.marcacoes.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MarcacaoSecretariaRepositoryTest {

    @Test
    @DisplayName("MarcacaoSecretariaRepository deve existir")
    void repositoryDeveExistir() {

        MarcacaoSecretariaRepository repository =
                mock(MarcacaoSecretariaRepository.class);

        assertNotNull(repository);
    }

    @Test
    @DisplayName("Classe MarcacaoSecretariaRepository deve carregar")
    void classeDeveCarregar() {

        assertNotNull(MarcacaoSecretariaRepository.class);
    }
}