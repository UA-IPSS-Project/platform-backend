package pt.florinhas.candidaturas.dto;

// Domain
import pt.florinhas.candidaturas.domain.CandidaturaEstado;

// Lombok
import lombok.Data;

@Data
public class CandidaturaStatusUpdate {
    private CandidaturaEstado estado;
    private Boolean assinado;
}
