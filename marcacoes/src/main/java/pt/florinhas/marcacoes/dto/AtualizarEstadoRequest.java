package pt.florinhas.marcacoes.dto;

public class AtualizarEstadoRequest {
    private String novoEstado;
    private Long funcionarioId;

    // Getters e Setters
    public String getNovoEstado() { return novoEstado; }
    public void setNovoEstado(String novoEstado) { this.novoEstado = novoEstado; }

    public Long getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }
}