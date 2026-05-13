package pt.florinhas.common_data.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import pt.florinhas.common_data.TestCryptoConfig;
import pt.florinhas.common_data.TestJpaConfig;
import pt.florinhas.common_data.domain.Utente;

@DataJpaTest
@ContextConfiguration(classes = TestJpaConfig.class)
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.ANY
)
class UtenteRepositoryTest {

    @Autowired
    private UtenteRepository repository;

    @BeforeEach
    void setUp() {

        TestCryptoConfig.initCrypto();
    }

    @Test
    void findByEmail_DeveRetornarUtente() {

        Utente utente =
                new Utente();

        utente.setNome("Teste");
        utente.setNif("123456789");
        utente.setEmail("teste@teste.com");

        repository.save(utente);

        Optional<Utente> result =
                repository.findByEmail(
                        "teste@teste.com"
                );

        assertTrue(result.isPresent());

        assertEquals(
                "teste@teste.com",
                result.get().getEmail()
        );
    }

    @Test
    void existsByEmail_DeveRetornarTrue() {

        Utente utente =
                new Utente();

        utente.setNome("Teste");
        utente.setNif("987654321");
        utente.setEmail("exists@teste.com");

        repository.save(utente);

        assertTrue(
                repository.existsByEmail(
                        "exists@teste.com"
                )
        );
    }

    @Test
    void findByTelefone_DeveRetornarUtente() {

        Utente utente =
                new Utente();

        utente.setNome("Teste");
        utente.setNif("111222333");
        utente.setTelefone("912345678");

        repository.save(utente);

        Optional<Utente> result =
                repository.findByTelefone(
                        "912345678"
                );

        assertTrue(result.isPresent());
    }

    @Test
    void countByActivo_DeveContarAtivos() {

        Utente utente =
                new Utente();

        utente.setNome("Teste");
        utente.setNif("444555666");
        utente.setActivo(true);

        repository.save(utente);

        long total =
                repository.countByActivo(true);

        assertTrue(total > 0);
    }
}