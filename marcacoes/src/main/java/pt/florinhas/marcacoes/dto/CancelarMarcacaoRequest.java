package pt.florinhas.marcacoes.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para pedido de cancelamento de uma marcação.
 *
 * Usos típicos:
 *  - Receber do frontend o motivo do cancelamento e a identificação
 *    do funcionário que executa a operação (para auditoria/registo).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelarMarcacaoRequest {

    // Motivo textual do cancelamento, apresentado ao utente e registado para auditoria.
    private String motivo;

    // Identificador do funcionário que efetuou o cancelamento. 
    private Long funcionarioId;
}
