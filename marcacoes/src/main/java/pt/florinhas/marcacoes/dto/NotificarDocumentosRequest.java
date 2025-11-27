package pt.florinhas.marcacoes.dto;

public class NotificarDocumentosRequest {
    private String observacoes;
    private Long funcionarioId;

    // Getters e Setters
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Long getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }
}