package pt.florinhas.marcacoes.dto;

public class ConfirmarPresencaRequest {
    private Boolean presencaConfirmada;
    private Long funcionarioId;

    // Getters e Setters
    public Boolean getPresencaConfirmada() { return presencaConfirmada; }
    public void setPresencaConfirmada(Boolean presencaConfirmada) { this.presencaConfirmada = presencaConfirmada; }

    public Long getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }
}