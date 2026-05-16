package pt.florinhas.marcacoes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TermsStatusDTO {
    private int currentVersion;
    private Integer userVersion;
    private boolean needsAcceptance;
}
