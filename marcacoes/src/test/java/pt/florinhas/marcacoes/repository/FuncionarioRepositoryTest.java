package pt.florinhas.marcacoes.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import pt.florinhas.common_data.repository.FuncionarioRepository;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import pt.florinhas.common_data.security.HashUtil;

@DataJpaTest
class FuncionarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    private Funcionario funcionario;

    @BeforeEach
    void setUp() {
        funcionario = new Funcionario();
        funcionario.setNome("João Silva");
        funcionario.setEmail("joao@test.com");
        funcionario.setNif("123456789");
        funcionario.setTelefone("912345678");
        funcionario.setTipo(FuncionarioTipo.SECRETARIA);
        funcionario = entityManager.persist(funcionario);
        entityManager.flush();
    }

    @Test
    void findByNifHash_DeveRetornarFuncionario() {
        // Act
        Optional<Funcionario> resultado = funcionarioRepository.findByNifHash(HashUtil.sha256Hex("123456789"));

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("João Silva", resultado.get().getNome());
    }

    @Test
    void findByEmail_DeveRetornarFuncionario() {
        // Act
        List<Funcionario> resultado = funcionarioRepository.findByEmail("joao@test.com");

        // Assert
        assertFalse(resultado.isEmpty());
        assertEquals("123456789", resultado.get(0).getNif());
    }

    @Test
    void findByTipo_DeveRetornarFuncionariosPorTipo() {
        // Act
        List<Funcionario> resultado = funcionarioRepository.findByTipo(FuncionarioTipo.SECRETARIA);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(f -> f.getTipo() == FuncionarioTipo.SECRETARIA));
    }

    @Test
    void findByNomeContainingIgnoreCase_DeveRetornarFuncionarios() {
        // Act
        List<Funcionario> resultado = funcionarioRepository.findByNomeContainingIgnoreCase("joão");

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals("João Silva", resultado.get(0).getNome());
    }

    @Test
    void existsByNif_DeveRetornarTrue() {
        // Act
        boolean existe = funcionarioRepository.existsByNif("123456789");

        // Assert
        assertTrue(existe);
    }

    @Test
    void existsByNif_DeveRetornarFalse() {
        // Act
        boolean existe = funcionarioRepository.existsByNif("999999998");

        // Assert
        assertFalse(existe);
    }
}
