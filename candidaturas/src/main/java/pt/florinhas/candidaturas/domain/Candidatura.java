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
@Document(collection = "candidaturas")
public class Candidatura {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    private String formId; // ref. ao Form

    private Map<String, Object> respostas;

    private String estado;

    private Long criadoPor;
    private Instant criadoEm;
    
    // Resubmissões
    private Long atualizadoPor;
    private Instant atualizadoEm;
}