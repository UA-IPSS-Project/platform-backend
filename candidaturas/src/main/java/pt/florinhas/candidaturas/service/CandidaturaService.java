package pt.florinhas.candidaturas.service;

// Java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.Instant;

// Spring
import org.springframework.stereotype.Service;

// MongoDB
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import pt.florinhas.candidaturas.repository.*;
import pt.florinhas.candidaturas.domain.*;
import pt.florinhas.candidaturas.dto.*;
import pt.florinhas.common_data.exception.BadRequestException;

@Service
@AllArgsConstructor
@Slf4j
public class CandidaturaService {
    private final CandidaturaRepository candidaturaRepository;
    private final FormRepository formRepository;
    private final MongoTemplate mongoTemplate;

    public Candidatura createCandidatura(CandidaturaCreate dto, Long userId) {
        Form form = formRepository.findById(dto.getFormId()).orElse(null);
        if (form == null) {
            log.warn("Form with id {} does not exist.", dto.getFormId());
            throw new BadRequestException("Form with id " + dto.getFormId() + " does not exist.");
        }

        CandidaturaEstado estado = dto.getEstado() != null ? dto.getEstado() : CandidaturaEstado.PENDENTE;
        if (estado != CandidaturaEstado.RASCUNHO) {
            validateResponsesAgainstSchema(dto.getRespostas(), form.getPages());
        }

        Candidatura candidatura = new Candidatura();
        candidatura.setFormId(dto.getFormId());
        candidatura.setNif(dto.getNif());
        candidatura.setNome(dto.getNome());
        candidatura.setRespostas(dto.getRespostas());
        candidatura.setEstado(estado);
        candidatura.setCriadoPor(userId);
        candidatura.setCriadoEm(Instant.now());
        candidatura.setAssinado(false);

        log.info("Creating application for NIF: {}", candidatura.getNif());
        return candidaturaRepository.save(candidatura);
    }

    public Candidatura updateCandidatura(String id, CandidaturaUpdate dto, Long userId) {
        Candidatura candidatura = candidaturaRepository.findById(id).orElse(null);
        if (candidatura == null) {
            log.error("Application with id {} does not exist.", id);
            return null;
        }

        Form form = formRepository.findById(candidatura.getFormId()).orElse(null);
        if (form == null) {
            log.warn("Form with id {} does not exist.", candidatura.getFormId());
            return null;
        }

        if (dto.getEstado() != CandidaturaEstado.RASCUNHO) {
            validateResponsesAgainstSchema(dto.getRespostas(), form.getPages());
        }

        candidatura.setRespostas(dto.getRespostas());
        if (dto.getEstado() != null) {
            candidatura.setEstado(dto.getEstado());
        }
        candidatura.setAtualizadoPor(userId);
        candidatura.setAtualizadoEm(Instant.now());

        log.info("Application updated date setting to now.");
        return candidaturaRepository.save(candidatura);
    }

    public Candidatura updateCandidaturaStatus(String id, CandidaturaStatusUpdate dto, Long userId) {
        Candidatura candidatura = candidaturaRepository.findById(id).orElse(null);
        if (candidatura == null) {
            log.error("Application with id {} does not exist.", id);
            return null;
        }

        if (dto.getEstado() != null) {
            candidatura.setEstado(dto.getEstado());
        }
        if (dto.getAssinado() != null) {
            candidatura.setAssinado(dto.getAssinado());
        }

        candidatura.setAtualizadoPor(userId);
        candidatura.setAtualizadoEm(Instant.now());

        log.info("Application status updated by user {}.", userId);
        return candidaturaRepository.save(candidatura);
    }

    public List<Candidatura> getCandidaturas(String nif, String nome, CandidaturaEstado estado, Boolean assinado,
            Integer idade) {
        Query query = new Query();

        if (nif != null) {
            query.addCriteria(Criteria.where("nif").is(nif));
        }
        if (nome != null) {
            query.addCriteria(Criteria.where("nome").regex(nome, "i"));
        }
        if (estado != null) {
            query.addCriteria(Criteria.where("estado").is(estado));
        }
        if (assinado != null) {
            query.addCriteria(Criteria.where("assinado").is(assinado));
        }
        if (idade != null) {
            query.addCriteria(Criteria.where("idade").is(idade));
        }

        return mongoTemplate.find(query, Candidatura.class);
    }

    public List<Candidatura> getCandidaturasByFormId(String formID) {
        return candidaturaRepository.findByFormId(formID);
    }

    public List<Candidatura> getCandidaturasByUserId(Long userID) {
        return candidaturaRepository.findByCriadoPor(userID);
    }

    public Candidatura getCandidaturaById(String id) {
        Candidatura candidatura = candidaturaRepository.findById(id).orElse(null);
        if (candidatura == null) {
            log.warn("Application with id {} does not exist.", id);
            return null;
        }
        return candidatura;
    }

    public boolean anexarFicheiros() {
        // TODO
        return false;
    }

    public boolean deleteCandidatura(String id) {
        if (!candidaturaRepository.existsById(id)) {
            log.error("Application with id {} does not exist.", id);
            return false;
        }
        candidaturaRepository.deleteById(id);
        return true;
    }

    private void validateResponsesAgainstSchema(Map<String, Object> responses, List<FormPage> pages) {
        try {
            List<String> validKeys = new ArrayList<>();
            if (pages != null) {
                for (FormPage page : pages) {
                    if (page.getFields() != null) {
                        for (FieldDefinition field : page.getFields()) {
                            validKeys.add(field.getKey());

                            boolean isRequired = false;
                            if (field.getConfig() != null && field.getConfig().containsKey("required")) {
                                Object req = field.getConfig().get("required");
                                if (req instanceof Boolean) {
                                    isRequired = (Boolean) req;
                                }
                            }
                            if (isRequired
                                    && (!responses.containsKey(field.getKey()) || responses.get(field.getKey()) == null
                                            || responses.get(field.getKey()).toString().trim().isEmpty())) {
                                throw new BadRequestException(
                                        "O campo obrigatório '" + field.getKey() + "' não foi preenchido.");
                            }
                        }
                    }
                }
            }

            for (String key : responses.keySet()) {
                if (!validKeys.contains(key)) {
                    throw new BadRequestException("O campo '" + key + "' não faz parte deste formulário.");
                }
            }

        } catch (Exception e) {
            if (e instanceof BadRequestException)
                throw (BadRequestException) e;
            log.error("Error validating responses against form pages", e);
            throw new BadRequestException("Invalid response format or form configuration error");
        }
    }
}
