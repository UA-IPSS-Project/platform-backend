package pt.florinhas.marcacoes.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para notificação de documentos inválidos/incompletos associados a uma marcação.
 *
 * Usos típicos:
 *  - Enviar do frontend observações sobre o problema detetado na documentação
 *    e identificar o funcionário que efetuou a notificação (para auditoria).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificarDocumentosRequest {

    // Observações/mensagem a apresentar ao utente sobre os documentos. 
    private String observacoes;

    // Identificador do funcionário que regista a notificação. 
    private Long funcionarioId;
}
