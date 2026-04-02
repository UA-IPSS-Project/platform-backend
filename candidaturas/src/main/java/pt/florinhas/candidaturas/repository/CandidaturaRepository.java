package pt.florinhas.candidaturas.repository;

// Mongo
import org.springframework.data.mongodb.repository.MongoRepository;

// From this project
import pt.florinhas.candidaturas.domain.Candidatura;

// Java
import java.util.List;

public interface CandidaturaRepository extends MongoRepository<Candidatura, String> {
    List<Candidatura> findByFormId(String formId);

    List<Candidatura> findByCriadoPor(Long userId);
}
