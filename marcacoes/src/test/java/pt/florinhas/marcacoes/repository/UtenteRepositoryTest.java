package pt.florinhas.marcacoes.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import pt.florinhas.common_data.repository.UtenteRepository;

import pt.florinhas.common_data.domain.Utente;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import pt.florinhas.common_data.security.HashUtil;

@DataJpaTest
class UtenteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UtenteRepository utenteRepository;

    private Utente utente;

    @BeforeEach
    void setUp() {
        utente = new Utente();
        utente.setNome("Maria Santos");
        utente.setEmail("maria@test.com");
        utente.setNif("987654321");
        utente.setTelefone("912345678");
        utente = entityManager.persist(utente);
        entityManager.flush();
    }

    @Test
    void findByNifHash_DeveRetornarUtente() {
        // Act
        List<Utente> resultado = utenteRepository.findByNifHash(HashUtil.sha256Hex("987654321"));

        // Assert
        assertFalse(resultado.isEmpty());
        assertEquals("Maria Santos", resultado.get(0).getNome());
    }

    @Test
    void findByEmail_DeveRetornarUtente() {
        // Act
        Optional<Utente> resultado = utenteRepository.findByEmail("maria@test.com");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("987654321", resultado.get().getNif());
    }

    @Test
    void findByTelefone_DeveRetornarUtente() {
        // Act
        Optional<Utente> resultado = utenteRepository.findByTelefone("912345678");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Maria Santos", resultado.get().getNome());
    }

    @Test
    void findByNomeContainingIgnoreCase_DeveRetornarUtentes() {
        // Act
        List<Utente> resultado = utenteRepository.findByNomeContainingIgnoreCase("maria");

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals("Maria Santos", resultado.get(0).getNome());
    }

    @Test
    void existsByNif_DeveRetornarTrue() {
        // Act
        boolean existe = utenteRepository.existsByNif("987654321");

        // Assert
        assertTrue(existe);
    }

    @Test
    void existsByNif_DeveRetornarFalse() {
        // Act
        boolean existe = utenteRepository.existsByNif("000000000");

        // Assert
        assertFalse(existe);
    }

    @Test
    void existsByEmail_DeveRetornarTrue() {
        // Act
        boolean existe = utenteRepository.existsByEmail("maria@test.com");

        // Assert
        assertTrue(existe);
    }

    @Test
    void existsByEmail_DeveRetornarFalse() {
        // Act
        boolean existe = utenteRepository.existsByEmail("naoexiste@test.com");

        // Assert
        assertFalse(existe);
    }
}
