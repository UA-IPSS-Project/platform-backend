package pt.florinhas.marcacoes.dto;

/**
 * DTO para confirmar a presença do utente numa marcação.
 *
 * Usos típicos:
 *  - Receber do frontend a confirmação (true/false) da presença
 *    e o identificador do funcionário que registou essa confirmação.
 */
public class ConfirmarPresencaRequest {

    // Indica se a presença foi confirmada (true) ou marcada como ausente (false). 
    private Boolean presencaConfirmada;

    // Identificador do funcionário que registou a confirmação/ausência. 
    private Long funcionarioId;

    // ================= Getters e Setters =================

    // return flag de presença confirmada. 
    public Boolean getPresencaConfirmada() { return presencaConfirmada; }

    // Define a flag de presença confirmada. 
    public void setPresencaConfirmada(Boolean presencaConfirmada) { this.presencaConfirmada = presencaConfirmada; }

    // return ID do funcionário que registou a presença/ausência. 
    public Long getFuncionarioId() { return funcionarioId; }

    // Define o ID do funcionário que registou a presença/ausência. 
    public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }
}
