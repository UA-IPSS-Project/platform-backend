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
    private String title;
    private String description;
    private List<String> fields;
}
