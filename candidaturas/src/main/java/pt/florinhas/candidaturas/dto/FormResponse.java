package pt.florinhas.candidaturas.dto;

// Java
import java.time.Instant;
import java.util.List;
import java.util.Map;

// Lombok
import lombok.Data;

// Domain
import pt.florinhas.candidaturas.domain.Form;
import pt.florinhas.candidaturas.domain.FormPage;

@Data
public class FormResponse {
    private String id;
    private String name;
    private Map<String, Object> schema;
    private Map<String, Object> uiSchema;
    private List<FormPage> pages;
    private Long criadoPor;
    private Instant criadoEm;
    private Long atualizadoPor;
    private Instant atualizadoEm;

    public static FormResponse fromEntity(Form form) {
        if (form == null)
            return null;
        FormResponse dto = new FormResponse();
        dto.setId(form.getId());
        dto.setName(form.getName());
        dto.setSchema(form.getSchema());
        dto.setUiSchema(form.getUiSchema());
        dto.setPages(form.getPages());
        dto.setCriadoPor(form.getCriadoPor());
        dto.setCriadoEm(form.getCriadoEm());
        dto.setAtualizadoPor(form.getAtualizadoPor());
        dto.setAtualizadoEm(form.getAtualizadoEm());
        return dto;
    }
}
