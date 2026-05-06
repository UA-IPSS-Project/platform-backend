package pt.florinhas.candidaturas.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import pt.florinhas.candidaturas.domain.FormPage;
import pt.florinhas.candidaturas.domain.FormStatus;

@Data
public class FormUpdate {
    @NotBlank(message = "Name is required")
    private String name;

    private FormStatus status;

    private List<FormPage> pages;
}
