package pt.florinhas.candidaturas.dto;

import java.util.Map;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CandidaturaUpdate {
    @NotNull(message = "Respostas are required")
    private Map<String, Object> respostas;
}
