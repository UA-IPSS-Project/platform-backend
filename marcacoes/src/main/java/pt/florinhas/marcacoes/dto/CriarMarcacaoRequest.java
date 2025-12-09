package pt.florinhas.marcacoes.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarMarcacaoRequest {
    private LocalDateTime data;
    private String assunto;
    private Long utenteId;
    private Long funcionarioId;
    private Long criadoPorId;
}