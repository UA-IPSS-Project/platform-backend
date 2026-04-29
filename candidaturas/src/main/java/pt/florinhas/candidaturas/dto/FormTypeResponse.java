package pt.florinhas.candidaturas.dto;

import lombok.Data;
import pt.florinhas.candidaturas.domain.Form;

@Data
public class FormTypeResponse {
    private String id;
    private String name;

    public static FormTypeResponse fromEntity(Form form) {
        if (form == null) return null;
        FormTypeResponse dto = new FormTypeResponse();
        dto.setId(form.getId());
        dto.setName(form.getName());
        return dto;
    }
}
