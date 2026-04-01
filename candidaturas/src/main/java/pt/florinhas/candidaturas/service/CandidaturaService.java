package pt.florinhas.candidaturas.service;

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
