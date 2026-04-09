package pt.florinhas.marcacoes.repository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    private Utente utente;
    private Funcionario funcionario;

    @BeforeEach
    void setUp() {
        utente = new Utente();
        utente.setNome("Maria Santos");
        utente.setEmail("maria@test.com");
        utente.setNif("123456789");
        utente.setTelefone("912345678");
        utente.setPassHash("hashedPassword");
        utente = entityManager.persist(utente);

        funcionario = new Funcionario();
        funcionario.setNome("João Silva");
        funcionario.setEmail("joao@test.com");
        funcionario.setNif("987654321");
        funcionario.setTelefone("912345679");
        funcionario.setPassHash("hashedPassword");
        funcionario.setTipo(FuncionarioTipo.SECRETARIA);
        funcionario = entityManager.persist(funcionario);

        entityManager.flush();
    }

    @Test
    void findByEmail_DeveRetornarUtilizador_QuandoEmailExiste() {
        // Act
        List<Utilizador> resultado = utilizadorRepository.findByEmail("maria@test.com");

        // Assert
        assertFalse(resultado.isEmpty());
        assertEquals("Maria Santos", resultado.get(0).getNome());
        assertEquals("123456789", resultado.get(0).getNif());
    }

    @Test
    void findByEmail_DeveRetornarEmpty_QuandoEmailNaoExiste() {
        // Act
        List<Utilizador> resultado = utilizadorRepository.findByEmail("naoexiste@test.com");

        // Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    void findByNif_DeveRetornarUtilizador_QuandoNifExiste() {
        // Act
        List<Utilizador> resultado = utilizadorRepository.findByNif("987654321");

        // Assert
        assertFalse(resultado.isEmpty());
        assertEquals("João Silva", resultado.get(0).getNome());
        assertTrue(resultado.get(0) instanceof Funcionario);
    }

    @Test
    void existsByEmail_DeveRetornarTrue_QuandoEmailExiste() {
        // Act
        boolean existe = utilizadorRepository.existsByEmail("maria@test.com");

        // Assert
        assertTrue(existe);
    }

    @Test
    void existsByEmail_DeveRetornarFalse_QuandoEmailNaoExiste() {
        // Act
        boolean existe = utilizadorRepository.existsByEmail("naoexiste@test.com");

        // Assert
        assertFalse(existe);
    }

    @Test
    void existsByNif_DeveRetornarTrue_QuandoNifExiste() {
        // Act
        boolean existe = utilizadorRepository.existsByNif("123456789");

        // Assert
        assertTrue(existe);
    }

    @Test
    void existsByNif_DeveRetornarFalse_QuandoNifNaoExiste() {
        // Act
        boolean existe = utilizadorRepository.existsByNif("000000000");

        // Assert
        assertFalse(existe);
    }

    @Test
    void findByNomeContainingIgnoreCase_DeveRetornarUtilizadores() {
        // Act
        List<Utilizador> resultado = utilizadorRepository.findByNomeContainingIgnoreCase("maria");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Maria Santos", resultado.get(0).getNome());
    }

    @Test
    void findByNomeContainingIgnoreCase_DeveSerCaseInsensitive() {
        // Act
        List<Utilizador> resultado = utilizadorRepository.findByNomeContainingIgnoreCase("MARIA");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    void findByTelefone_DeveRetornarUtilizador_QuandoTelefoneExiste() {
        // Act
        Optional<Utilizador> resultado = utilizadorRepository.findByTelefone("912345678");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Maria Santos", resultado.get().getNome());
    }

    @Test
    void existsByTelefone_DeveRetornarTrue_QuandoTelefoneExiste() {
        // Act
        boolean existe = utilizadorRepository.existsByTelefone("912345679");

        // Assert
        assertTrue(existe);
    }
}
