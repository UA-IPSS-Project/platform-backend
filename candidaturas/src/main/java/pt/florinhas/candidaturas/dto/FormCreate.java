package pt.florinhas.candidaturas.dto;

import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pt.florinhas.candidaturas.domain.FormPage;

@Data
public class FormCreate {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Schema is required")
    private Map<String, Object> schema;

    private Map<String, Object> uiSchema;
    private List<FormPage> pages;
}
