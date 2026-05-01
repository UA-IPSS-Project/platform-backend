package pt.florinhas.marcacoes.repository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;

@DataJpaTest
class UtilizadorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UtilizadorRepository utilizadorRepository;

    // Fixed hashes used in tests — value doesn't matter, just needs to be consistent
    private static final String UTENTE_NIF_HASH = "hash_utente_123456789";
    private static final String FUNCIONARIO_NIF_HASH = "hash_funcionario_987654321";

    @BeforeEach
    void setUp() {
        Utente utente = new Utente();
        utente.setNome("Maria Santos");
        utente.setEmail("maria@test.com");
        utente.setNif("CIFRADO_123456789"); // simula valor cifrado
        utente.setNifHash(UTENTE_NIF_HASH);
        utente.setTelefone("912345678");
        utente.setPassHash("hashedPassword");
        entityManager.persist(utente);

        Funcionario funcionario = new Funcionario();
        funcionario.setNome("João Silva");
        funcionario.setEmail("joao@test.com");
        funcionario.setNif("CIFRADO_987654321");
        funcionario.setNifHash(FUNCIONARIO_NIF_HASH);
        funcionario.setTelefone("912345679");
        funcionario.setPassHash("hashedPassword");
        funcionario.setTipo(FuncionarioTipo.SECRETARIA);
        entityManager.persist(funcionario);

        entityManager.flush();
    }

    @Test
    void findByEmail_DeveRetornarUtilizador_QuandoEmailExiste() {
        List<Utilizador> resultado = utilizadorRepository.findByEmail("maria@test.com");
        assertFalse(resultado.isEmpty());
        assertEquals("Maria Santos", resultado.get(0).getNome());
    }

    @Test
    void findByEmail_DeveRetornarEmpty_QuandoEmailNaoExiste() {
        assertTrue(utilizadorRepository.findByEmail("naoexiste@test.com").isEmpty());
    }

    @Test
    void findByNifHash_DeveRetornarUtilizador_QuandoHashExiste() {
        List<Utilizador> resultado = utilizadorRepository.findByNifHash(FUNCIONARIO_NIF_HASH);
        assertFalse(resultado.isEmpty());
        assertEquals("João Silva", resultado.get(0).getNome());
        assertInstanceOf(Funcionario.class, resultado.get(0));
    }

    @Test
    void existsByEmail_DeveRetornarTrue_QuandoEmailExiste() {
        assertTrue(utilizadorRepository.existsByEmail("maria@test.com"));
    }

    @Test
    void existsByEmail_DeveRetornarFalse_QuandoEmailNaoExiste() {
        assertFalse(utilizadorRepository.existsByEmail("naoexiste@test.com"));
    }

    @Test
    void existsByNifHash_DeveRetornarTrue_QuandoHashExiste() {
        assertTrue(utilizadorRepository.existsByNifHash(UTENTE_NIF_HASH));
    }

    @Test
    void existsByNifHash_DeveRetornarFalse_QuandoHashNaoExiste() {
        assertFalse(utilizadorRepository.existsByNifHash("hash_inexistente"));
    }

    @Test
    void findByNomeContainingIgnoreCase_DeveRetornarUtilizadores() {
        List<Utilizador> resultado = utilizadorRepository.findByNomeContainingIgnoreCase("maria");
        assertEquals(1, resultado.size());
        assertEquals("Maria Santos", resultado.get(0).getNome());
    }

    @Test
    void findByTelefone_DeveRetornarUtilizador_QuandoTelefoneExiste() {
        Optional<Utilizador> resultado = utilizadorRepository.findByTelefone("912345678");
        assertTrue(resultado.isPresent());
        assertEquals("Maria Santos", resultado.get().getNome());
    }

    @Test
    void existsByTelefone_DeveRetornarTrue_QuandoTelefoneExiste() {
        assertTrue(utilizadorRepository.existsByTelefone("912345679"));
    }
}
