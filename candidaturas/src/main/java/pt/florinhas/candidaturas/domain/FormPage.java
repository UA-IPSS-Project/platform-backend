package pt.florinhas.candidaturas.domain;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FormPage {
    private String id;
    private String title;
    private String description;
    private int order;
    private List<FieldDefinition> fields;
}
