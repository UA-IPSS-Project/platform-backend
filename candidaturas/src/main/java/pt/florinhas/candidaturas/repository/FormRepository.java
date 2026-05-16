package pt.florinhas.candidaturas.repository;

// Mongo
import org.springframework.data.mongodb.repository.MongoRepository;

// Java
import java.util.List;

// From this project
import pt.florinhas.candidaturas.domain.Form;
import pt.florinhas.candidaturas.domain.FormStatus;

public interface FormRepository extends MongoRepository<Form, String> {
    boolean existsByName(String name);

    Form findByName(String name);

    List<Form> findByStatus(FormStatus status);
}
