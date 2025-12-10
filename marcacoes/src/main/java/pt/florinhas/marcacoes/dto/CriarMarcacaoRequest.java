package pt.florinhas.marcacoes.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarMarcacaoRequest {
    private LocalDateTime data;
    private String assunto;
    private String descricao;
    private Long utenteId;
    private Long funcionarioId;
    private Long criadoPorId;
    
    // Campos para criação de utente (se não existir)
    private String utenteNif;
    private String utenteNome;
    private String utenteEmail;
    private String utenteTelefone;
}