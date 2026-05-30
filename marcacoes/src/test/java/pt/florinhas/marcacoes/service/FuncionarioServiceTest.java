package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.repository.FuncionarioRepository;

class FuncionarioServiceTest {

    private FuncionarioRepository funcionarioRepository;
    private FuncionarioService service;

    @BeforeEach
    void setUp() {
        funcionarioRepository = mock(FuncionarioRepository.class);
        service = new FuncionarioService(funcionarioRepository);
    }

    @Test
    void listarSecretarias_DeveRetornarSecretarias() {

        Funcionario funcionario = new Funcionario();
        funcionario.setTipo(FuncionarioTipo.SECRETARIA);

        when(funcionarioRepository.findByTipo(FuncionarioTipo.SECRETARIA))
                .thenReturn(List.of(funcionario));

        List<Funcionario> result = service.listarSecretarias();

        assertEquals(1, result.size());
        assertEquals(FuncionarioTipo.SECRETARIA, result.get(0).getTipo());
    }
}