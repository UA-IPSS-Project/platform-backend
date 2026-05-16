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

@org.junit.jupiter.api.Disabled
@DataJpaTest
class UtenteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UtenteRepository utenteRepository;

    private static final String NIF_HASH = "hash_utente_987654321";

    @BeforeEach
    void setUp() {
        Utente utente = new Utente();
        utente.setNome("Maria Santos");
        utente.setEmail("maria@test.com");
        utente.setNif("CIFRADO_987654321");
        utente.setNifHash(NIF_HASH);
        utente.setTelefone("912345678");
        entityManager.persist(utente);
        entityManager.flush();
    }

    @Test
    void findByNifHash_DeveRetornarUtente() {
        List<Utente> resultado = utenteRepository.findByNifHash(NIF_HASH);
        assertFalse(resultado.isEmpty());
        assertEquals("Maria Santos", resultado.get(0).getNome());
    }

    @Test
    void findByEmail_DeveRetornarUtente() {
        Optional<Utente> resultado = utenteRepository.findByEmail("maria@test.com");
        assertTrue(resultado.isPresent());
        assertEquals(NIF_HASH, resultado.get().getNifHash());
    }

    @Test
    void findByTelefone_DeveRetornarUtente() {
        Optional<Utente> resultado = utenteRepository.findByTelefone("912345678");
        assertTrue(resultado.isPresent());
        assertEquals("Maria Santos", resultado.get().getNome());
    }

    @Test
    void findByNomeContainingIgnoreCase_DeveRetornarUtentes() {
        List<Utente> resultado = utenteRepository.findByNomeContainingIgnoreCase("maria");
        assertFalse(resultado.isEmpty());
        assertEquals("Maria Santos", resultado.get(0).getNome());
    }

    @Test
    void existsByNifHash_DeveRetornarTrue() {
        assertTrue(utenteRepository.existsByNifHash(NIF_HASH));
    }

    @Test
    void existsByNifHash_DeveRetornarFalse() {
        assertFalse(utenteRepository.existsByNifHash("hash_inexistente"));
    }

    @Test
    void existsByEmail_DeveRetornarTrue() {
        assertTrue(utenteRepository.existsByEmail("maria@test.com"));
    }

    @Test
    void existsByEmail_DeveRetornarFalse() {
        assertFalse(utenteRepository.existsByEmail("naoexiste@test.com"));
    }
}
