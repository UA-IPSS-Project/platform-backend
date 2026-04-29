package pt.florinhas.candidaturas.domain;

// Java
import java.time.Instant;
import java.util.Map;

// Mongo
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

// Lombok
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;

@Getter
@Setter
@Document(collection = "forms")
public class Form {
    @Id
    @Setter(AccessLevel.NONE) // To not allow ID changes
    private String id;

    private String name;

    private Map<String, Object> schema;
    private Map<String, Object> uiSchema;

    private Long criadoPor;
    private Instant criadoEm;
    private Long atualizadoPor;
    private Instant atualizadoEm;
}
