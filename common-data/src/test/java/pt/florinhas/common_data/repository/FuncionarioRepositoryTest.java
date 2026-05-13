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
import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;

@DataJpaTest
@ContextConfiguration(classes = TestJpaConfig.class)
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.ANY
)
class FuncionarioRepositoryTest {

    @Autowired
    private FuncionarioRepository repository;

    @BeforeEach
    void setUp() {

        TestCryptoConfig.initCrypto();
    }

    @Test
    void findByTipo_DeveRetornarFuncionarios() {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setNome("Funcionario");
        funcionario.setNif("123456789");
        funcionario.setEmail("func@teste.com");
        funcionario.setTipo(
                FuncionarioTipo.SECRETARIA
        );

        repository.save(funcionario);

        List<Funcionario> result =
                repository.findByTipo(
                        FuncionarioTipo.SECRETARIA
                );

        assertFalse(result.isEmpty());
    }

    @Test
    void existsByEmail_DeveRetornarTrue() {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setNome("Funcionario");
        funcionario.setNif("987654321");
        funcionario.setEmail("func2@teste.com");

        repository.save(funcionario);

        assertTrue(
                repository.existsByEmail(
                        "func2@teste.com"
                )
        );
    }

    @Test
    void findByActivoFalse_DeveRetornarPendentes() {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setNome("Funcionario");
        funcionario.setNif("111222333");
        funcionario.setEmail("inactive@teste.com");
        funcionario.setActivo(false);

        repository.save(funcionario);

        List<Funcionario> result =
                repository.findByActivoFalse();

        assertFalse(result.isEmpty());
    }
}