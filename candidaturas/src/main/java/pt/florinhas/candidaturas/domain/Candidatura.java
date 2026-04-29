package pt.florinhas.candidaturas.domain;

// Java
import java.time.Instant;
import java.util.Map;

// Mongo
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.data.annotation.Id;

// Validation
import jakarta.validation.constraints.*;

// Lombok
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "candidaturas")
public class Candidatura {

    @Id
    private String id;

    @NotBlank(message = "formId is required")
    private String formId; // ref. to Form

    @NotBlank(message = "NIF is required")
    private String nif;

    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÂÊÎÔÛâêîôûÃÑÕãñõÇç ]*$", message = "Invalid name")
    private String nome;

    private Map<String, Object> respostas;

    @NotNull(message = "Estado is required")
    private CandidaturaEstado estado;

    @NotNull(message = "criadoPor is required")
    private Long criadoPor;

    @NotNull(message = "criadoEm is required")
    private Instant criadoEm;

    private Boolean assinado;

    private Integer ranking;

    // Resubmissions
    private Long atualizadoPor;
    private Instant atualizadoEm;
}