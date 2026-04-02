package pt.florinhas.candidaturas.service;

import java.util.List;

// Spring
import org.springframework.stereotype.Service;

// Lombok
import lombok.AllArgsConstructor;

// From this project
import pt.florinhas.candidaturas.repository.*;
import pt.florinhas.candidaturas.domain.*;

@Service
@AllArgsConstructor
public class CandidaturaService {
    private final CandidaturaRepository candidaturaRepository;
    private final FormRepository formRepository;

    public Candidatura createCandidatura(Candidatura candidatura) {

        if (!isCandidaturaValid(candidatura)) {
            throw new IllegalArgumentException("Candidatura is not valid. FormId and respostas are required.");
        }

        // Necessário validar os campos da candidatur com base no schema do form

        return candidaturaRepository.save(candidatura);
    }

    public Candidatura updateCandidatura(String id, Candidatura candidatura) {

        if (!isCandidaturaValid(candidatura)) {
            throw new IllegalArgumentException("Candidatura is not valid. FormId and respostas are required.");
        }

        if (!candidaturaRepository.existsById(id)) {
            throw new IllegalArgumentException("Candidatura with id " + id + " does not exist.");
        }

        return candidaturaRepository.save(candidatura);
    }

    public List<Candidatura> getCandidaturas() {
        return candidaturaRepository.findAll();
    }

    public List<Candidatura> getCandidaturasByFormId(String formID) {
        return candidaturaRepository.findByFormId(formID);
    }

    public List<Candidatura> getCandidaturasByUserId(Long userID) {
        return candidaturaRepository.findByCriadoPor(userID);
    }

    public Candidatura getCandidaturaById(String id) {
        return candidaturaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidatura with id " + id + " does not exist."));    
    }

    public boolean anexarFicheiros() {
        // TODO
        return false;
    }

    public boolean deleteCandidatura(String id) {
        if (!candidaturaRepository.existsById(id)) {
            throw new IllegalArgumentException("Candidatura with id " + id + " does not exist.");
        }

        candidaturaRepository.deleteById(id);
        return true;
    }

    // Verificar se é tem FormId válido e as respostas não estão vazias
    private boolean isCandidaturaValid(Candidatura candidatura) {
        if (candidatura.getFormId() == null || candidatura.getFormId().isEmpty()) {
            return false;
        }

        if (!formRepository.existsById(candidatura.getFormId())) {
            return false;
        }

        if (candidatura.getRespostas() == null || candidatura.getRespostas().isEmpty()) {
            return false;
        }

        return true;
    }
}
