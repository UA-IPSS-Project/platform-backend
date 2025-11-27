package pt.florinhas.marcacoes.dto;

public class AtualizarConsumosRequest {
    private Integer quantidadeProdutos;
    private Integer quantidadeRoupa;
    private String observacoesConsumo;
    private Long funcionarioId;

    // Getters e Setters
    public Integer getQuantidadeProdutos() { return quantidadeProdutos; }
    public void setQuantidadeProdutos(Integer quantidadeProdutos) { this.quantidadeProdutos = quantidadeProdutos; }

    public Integer getQuantidadeRoupa() { return quantidadeRoupa; }
    public void setQuantidadeRoupa(Integer quantidadeRoupa) { this.quantidadeRoupa = quantidadeRoupa; }

    public String getObservacoesConsumo() { return observacoesConsumo; }
    public void setObservacoesConsumo(String observacoesConsumo) { this.observacoesConsumo = observacoesConsumo; }

    public Long getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }
}