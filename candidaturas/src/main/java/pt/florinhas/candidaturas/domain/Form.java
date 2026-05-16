package pt.florinhas.candidaturas.domain;

// Java
import java.time.Instant;
import java.util.List;

// Mongo
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;

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

    @Indexed(unique = true)
    private String name;

    @Version
    private Long version;

    private FormStatus status;

    private List<FormPage> pages;

    // Rastreamento
    private Long criadoPor;
    private String criadoPorNome;
    private Instant criadoEm;
    private Long atualizadoPor;
    private String atualizadoPorNome;
    private Instant atualizadoEm;
}
