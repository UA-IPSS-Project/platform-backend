package pt.florinhas.marcacoes.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@org.junit.jupiter.api.Disabled
@DataJpaTest
class FuncionarioRepositoryTest {

    private final TestEntityManager entityManager;
    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioRepositoryTest(TestEntityManager entityManager, FuncionarioRepository funcionarioRepository) {
        this.entityManager = entityManager;
        this.funcionarioRepository = funcionarioRepository;
    }

    private static final String NIF_HASH = "hash_funcionario_123456789";

    @BeforeEach
    void setUp() {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome("João Silva");
        funcionario.setEmail("joao@test.com");
        funcionario.setNif("CIFRADO_123456789");
        funcionario.setNifHash(NIF_HASH);
        funcionario.setTelefone("912345678");
        funcionario.setTipo(FuncionarioTipo.SECRETARIA);
        entityManager.persist(funcionario);
        entityManager.flush();
    }

    @Test
    void findByNifHash_DeveRetornarFuncionario() {
        Optional<Funcionario> resultado = funcionarioRepository.findByNifHash(NIF_HASH);
        assertTrue(resultado.isPresent());
        assertEquals("João Silva", resultado.get().getNome());
    }

    @Test
    void findByEmail_DeveRetornarFuncionario() {
        List<Funcionario> resultado = funcionarioRepository.findByEmail("joao@test.com");
        assertFalse(resultado.isEmpty());
        assertEquals(NIF_HASH, resultado.get(0).getNifHash());
    }

    @Test
    void findByTipo_DeveRetornarFuncionariosPorTipo() {
        List<Funcionario> resultado = funcionarioRepository.findByTipo(FuncionarioTipo.SECRETARIA);
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(f -> f.getTipo() == FuncionarioTipo.SECRETARIA));
    }

    @Test
    void findByNomeContainingIgnoreCase_DeveRetornarFuncionarios() {
        List<Funcionario> resultado = funcionarioRepository.findByNomeContainingIgnoreCase("joão");
        assertFalse(resultado.isEmpty());
        assertEquals("João Silva", resultado.get(0).getNome());
    }

    @Test
    void existsByNifHash_DeveRetornarTrue() {
        assertTrue(funcionarioRepository.existsByNifHash(NIF_HASH));
    }

    @Test
    void existsByNifHash_DeveRetornarFalse() {
        assertFalse(funcionarioRepository.existsByNifHash("hash_inexistente"));
    }
}
