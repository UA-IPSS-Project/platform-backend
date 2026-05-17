package pt.florinhas.candidaturas.dto;

// Java
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

// Lombok
import lombok.Data;

// Domain
import pt.florinhas.candidaturas.domain.FieldAudience;
import pt.florinhas.candidaturas.domain.FieldDefinition;
import pt.florinhas.candidaturas.domain.Form;
import pt.florinhas.candidaturas.domain.FormPage;
import pt.florinhas.candidaturas.domain.FormStatus;

@Data
public class FormResponse {
    private String id;
    private String name;
    private Long version;
    private FormStatus status;
    private List<FormPage> pages;
    private Long criadoPor;
    private String criadoPorNome;
    private Instant criadoEm;
    private Long atualizadoPor;
    private String atualizadoPorNome;
    private Instant atualizadoEm;

    public static FormResponse fromEntity(Form form) {
        return fromEntity(form, true);
    }

    public static FormResponse fromEntity(Form form, boolean includeInternal) {
        if (form == null)
            return null;
        FormResponse dto = new FormResponse();
        dto.setId(form.getId());
        dto.setName(form.getName());
        dto.setVersion(form.getVersion());
        dto.setStatus(form.getStatus());
        dto.setPages(filterPages(form.getPages(), includeInternal));
        dto.setCriadoPor(form.getCriadoPor());
        dto.setCriadoPorNome(form.getCriadoPorNome());
        dto.setCriadoEm(form.getCriadoEm());
        dto.setAtualizadoPor(form.getAtualizadoPor());
        dto.setAtualizadoPorNome(form.getAtualizadoPorNome());
        dto.setAtualizadoEm(form.getAtualizadoEm());
        return dto;
    }

    private static List<FormPage> filterPages(List<FormPage> pages, boolean includeInternal) {
        if (pages == null) return null;
        return pages.stream()
                .filter(p -> includeInternal || p.getAudience() != FieldAudience.INTERNAL)
                .map(p -> filterPageFields(p, includeInternal))
                .collect(Collectors.toList());
    }

    private static FormPage filterPageFields(FormPage page, boolean includeInternal) {
        if (includeInternal || page.getFields() == null) return page;
        List<FieldDefinition> visibleFields = page.getFields().stream()
                .filter(f -> f.getAudience() != FieldAudience.INTERNAL)
                .collect(Collectors.toList());
        FormPage filtered = new FormPage();
        filtered.setId(page.getId());
        filtered.setTitle(page.getTitle());
        filtered.setDescription(page.getDescription());
        filtered.setOrder(page.getOrder());
        filtered.setAudience(page.getAudience());
        filtered.setFields(visibleFields);
        return filtered;
    }
}
