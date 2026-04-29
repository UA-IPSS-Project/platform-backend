package pt.florinhas.candidaturas.service;

// Java
import java.util.List;
import java.util.Map;
import java.time.Instant;

// Spring
import org.springframework.stereotype.Service;

// MongoDB
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

// Lombok
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// From this project
import pt.florinhas.candidaturas.repository.*;
import pt.florinhas.candidaturas.domain.*;
import pt.florinhas.common_data.validation.NifValidator;
import pt.florinhas.common_data.exception.BadRequestException;

// JSON Schema Validation
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class CandidaturaService {
    private final CandidaturaRepository candidaturaRepository;
    private final FormRepository formRepository;
    private final MongoTemplate mongoTemplate;

    private final NifValidator nifValidator;
    private final ObjectMapper objectMapper;

    public Candidatura createCandidatura(Candidatura candidatura) {
        if (!isCandidaturaValid(candidatura)) {
            return null;
        }

        Form form = formRepository.findById(candidatura.getFormId()).get();

        if (form == null) {
            log.warn("Form with id {} does not exist.", candidatura.getFormId());
            return null;
        }

        validateResponsesAgainstSchema(candidatura.getRespostas(), form.getSchema());

        if (candidatura.getCriadoEm() == null) {
            log.info("Application created date setting to now.");
            candidatura.setCriadoEm(Instant.now());
        }

        log.info("Creating application for NIF: {}", candidatura.getNif());

        candidatura.setEstado(CandidaturaEstado.PENDENTE);
        candidatura.setAssinado(false); // Verificar a existência de um documento assinado

        return candidaturaRepository.save(candidatura);
    }

    public Candidatura updateCandidatura(String id, Candidatura candidatura) {
        if (!isCandidaturaValid(candidatura)) {
            return null;
        }

        if (candidatura.getAtualizadoPor() == null) {
            log.warn("UpdatedBy is required.");
            return null;
        }

        Form form = formRepository.findById(candidatura.getFormId()).get();

        if (form == null) {
            log.warn("Form with id {} does not exist.", candidatura.getFormId());
            return null;
        }

        validateResponsesAgainstSchema(candidatura.getRespostas(), form.getSchema());

        if (candidatura.getAtualizadoEm() == null) {
            log.info("Application updated date setting to now.");
            candidatura.setAtualizadoEm(Instant.now());
        }

        if (!candidaturaRepository.existsById(id)) {
            log.error("Application with id {} does not exist.", id);
            return null;
        }

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

    // Check if Application has a valid FormId and responses are not empty
    private boolean isCandidaturaValid(Candidatura candidatura) {

        if (!nifValidator.isValidRequired(candidatura.getNif())) {
            log.warn("NIF is not valid.");
            return false;
        }

        if (candidatura.getNome() == null || candidatura.getNome().trim().isEmpty()) {
            log.warn("Name is required.");
            return false;
        }

        if (candidatura.getFormId() == null || candidatura.getFormId().isEmpty()) {
            log.warn("FormId is required.");
            return false;
        }

        if (!formRepository.existsById(candidatura.getFormId())) {
            log.warn("Form with id {} does not exist.", candidatura.getFormId());
            return false;
        }

        if (candidatura.getRespostas() == null || candidatura.getRespostas().isEmpty()) {
            log.warn("Answers are required.");
            return false;
        }

        if (candidatura.getCriadoPor() == null) {
            log.warn("CreatedBy is required.");
            return false;
        }

        return true;
    }

    private void validateResponsesAgainstSchema(Map<String, Object> responses, Map<String, Object> schema) {
        try {
            JsonNode responsesNode = objectMapper.valueToTree(responses);
            JsonNode schemaNode = objectMapper.valueToTree(schema);

            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema jsonSchema = factory.getSchema(schemaNode);

            Set<ValidationMessage> errors = jsonSchema.validate(responsesNode);

            if (!errors.isEmpty()) {
                StringBuilder sb = new StringBuilder("Validation errors in responses: ");
                errors.forEach(err -> sb.append(err.getMessage()).append("; "));
                throw new BadRequestException(sb.toString());
            }
        } catch (Exception e) {
            if (e instanceof BadRequestException)
                throw (BadRequestException) e;
            log.error("Error validating responses against schema", e);
            throw new BadRequestException("Invalid response format or schema error");
        }
    }
}
