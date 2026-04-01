package pt.florinhas.candidaturas.repository;

// Mongo
import org.springframework.data.mongodb.repository.MongoRepository;

// From this project
import pt.florinhas.candidaturas.domain.Candidatura;

public interface CandidaturaRepository extends MongoRepository<Candidatura, String> {
}
