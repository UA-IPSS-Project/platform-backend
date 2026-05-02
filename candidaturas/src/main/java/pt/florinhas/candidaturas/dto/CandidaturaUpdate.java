package pt.florinhas.candidaturas.dto;

import java.util.Map;
import jakarta.validation.constraints.NotNull;
import pt.florinhas.candidaturas.domain.CandidaturaEstado;
import lombok.Data;

@Data
public class CandidaturaUpdate {
    @NotNull(message = "Respostas are required")
    private Map<String, Object> respostas;

    private CandidaturaEstado estado;
}
