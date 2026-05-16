package pt.florinhas.marcacoes.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "assuntos", key = "'ativos'")
    public List<Assunto> listarAtivos() {
        return assuntoRepository.findByAtivoTrue();
    }

    @Cacheable(value = "assuntos", key = "'todos'")
    public List<Assunto> listarTodos() {
        return assuntoRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = "assuntos", allEntries = true)
    public Assunto criar(String nome) {
        String normalized = nome.trim().toLowerCase();
        if (assuntoRepository.findByNome(normalized).isPresent()) {
            throw new IllegalArgumentException("Já existe um assunto com este nome.");
        }

        Assunto assunto = new Assunto();
        assunto.setNome(normalized);
        assunto.setAtivo(true);
        return assuntoRepository.save(assunto);
    }

    @Transactional
    @CacheEvict(value = "assuntos", allEntries = true)
    public Assunto atualizar(Long id, String novoNome) {
        Assunto existindo = assuntoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assunto não encontrado"));
        
        if (novoNome != null) {
            String normalized = novoNome.trim().toLowerCase();
            assuntoRepository.findByNome(normalized).ifPresent(a -> {
                if (!a.getId().equals(id)) {
                    throw new IllegalArgumentException("Já existe um assunto com este nome.");
                }
            });
            existindo.setNome(normalized);
        }
        
        return assuntoRepository.save(existindo);
    }

    @Transactional
    @CacheEvict(value = "assuntos", allEntries = true)
    public void apagar(Long id) {
        Assunto existindo = assuntoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assunto não encontrado"));
        
        // Soft delete ou desativação
        existindo.setAtivo(false);
        assuntoRepository.save(existindo);
    }

    @Transactional
    @CacheEvict(value = "assuntos", allEntries = true)
    public Assunto setAtivo(Long id, boolean ativo) {
        Assunto existindo = assuntoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assunto não encontrado"));
        
        existindo.setAtivo(ativo);
        return assuntoRepository.save(existindo);
    }
}