package pt.florinhas.candidaturas.repository;

// Mongo
import org.springframework.data.mongodb.repository.MongoRepository;

// From this project
import pt.florinhas.candidaturas.domain.Form;

public interface FormRepository extends MongoRepository<Form, String> {
    boolean existsByName(String name);

    Form findByName(String name);
}
