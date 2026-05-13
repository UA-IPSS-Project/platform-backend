package pt.florinhas.common_data.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import pt.florinhas.common_data.TestCryptoConfig;
import pt.florinhas.common_data.TestJpaConfig;
import pt.florinhas.common_data.domain.Utilizador;

@DataJpaTest
@ContextConfiguration(classes = TestJpaConfig.class)
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.ANY
)
class UtilizadorRepositoryTest {

    @Autowired
    private UtilizadorRepository repository;

    @BeforeEach
    void setUp() {

        TestCryptoConfig.initCrypto();
    }

    @Test
    void findByEmail_DeveRetornarLista() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setNome("Teste");
        utilizador.setNif("123456789");
        utilizador.setEmail("teste@teste.com");

        repository.save(utilizador);

        List<Utilizador> result =
                repository.findByEmail(
                        "teste@teste.com"
                );

        assertFalse(result.isEmpty());
    }

    @Test
    void existsByEmail_DeveRetornarTrue() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setNome("Teste");
        utilizador.setNif("987654321");
        utilizador.setEmail("exists@teste.com");

        repository.save(utilizador);

        assertTrue(
                repository.existsByEmail(
                        "exists@teste.com"
                )
        );
    }

    @Test
    void findByNomeContainingIgnoreCase_DeveEncontrar() {

        Utilizador utilizador =
                new Utilizador();

        utilizador.setNome("Joao");
        utilizador.setNif("111222333");

        repository.save(utilizador);

        List<Utilizador> result =
                repository.findByNomeContainingIgnoreCase(
                        "jo"
                );

        assertFalse(result.isEmpty());
    }
}