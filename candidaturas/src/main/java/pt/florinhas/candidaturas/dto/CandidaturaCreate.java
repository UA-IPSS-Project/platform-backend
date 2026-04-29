package pt.florinhas.candidaturas.dto;

// Java
import java.util.Map;

// Validation
import jakarta.validation.constraints.*;

// Common Data
import pt.florinhas.common_data.validation.Nif;

// Lombok
import lombok.Data;

@Data
public class CandidaturaCreate {
    @NotBlank(message = "formId is required")
    private String formId;

    @NotBlank(message = "NIF is required")
    @Nif
    private String nif;

    @NotBlank(message = "Name is required")
    private String nome;

    @NotNull(message = "Respostas are required")
    private Map<String, Object> respostas;
}
