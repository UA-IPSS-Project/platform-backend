package pt.florinhas.marcacoes.dto;

public class CancelarMarcacaoRequest {
    private String motivo;
    private Long funcionarioId;

    // Getters e Setters
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Long getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }
}