package pt.florinhas.marcacoes.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.florinhas.marcacoes.domain.Assunto;
import pt.florinhas.marcacoes.exception.NotFoundException;
import pt.florinhas.marcacoes.repository.AssuntoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssuntoService {

    private final AssuntoRepository assuntoRepository;

    public List<Assunto> listarAtivos() {
        return assuntoRepository.findByAtivoTrue();
    }

    public List<Assunto> listarTodos() {
        return assuntoRepository.findAll();
    }

    @Transactional
    public Assunto criar(Assunto assunto) {
        if (assunto.getNome() != null) {
            assunto.setNome(assunto.getNome().trim().toLowerCase());
        }
        return assuntoRepository.save(assunto);
    }

    @Transactional
    public Assunto atualizar(Long id, Assunto dados) {
        Assunto existindo = assuntoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assunto não encontrado"));
        
        if (dados.getNome() != null) {
            existindo.setNome(dados.getNome().trim().toLowerCase());
        }
        existindo.setAtivo(dados.isAtivo());
        
        return assuntoRepository.save(existindo);
    }

    @Transactional
    public void apagar(Long id) {
        Assunto existindo = assuntoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assunto não encontrado"));
        
        // Soft delete ou desativação
        existindo.setAtivo(false);
        assuntoRepository.save(existindo);
    }

    @Transactional
    public Assunto setAtivo(Long id, boolean ativo) {
        Assunto existindo = assuntoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assunto não encontrado"));
        
        existindo.setAtivo(ativo);
        return assuntoRepository.save(existindo);
    }
}
