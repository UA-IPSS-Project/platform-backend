package pt.florinhas.marcacoes.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.repository.FuncionarioRepository;

@Service
@RequiredArgsConstructor
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    @Cacheable("secretarias")
    @Transactional(readOnly = true)
    public List<Funcionario> listarSecretarias() {
        return funcionarioRepository.findByTipo(FuncionarioTipo.SECRETARIA);
    }
}
