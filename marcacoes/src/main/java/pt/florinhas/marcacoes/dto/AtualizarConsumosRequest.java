package pt.florinhas.marcacoes.dto;

/**
 * DTO para atualização dos consumos associados a uma marcação/atendimento.
 *
 * Usos típicos:
 *  - Receber do frontend a quantidade de produtos e peças de roupa consumidas,
 *    bem como observações e identificação do funcionário que registou o consumo.
 */
public class AtualizarConsumosRequest {

    // Quantidade de produtos consumidos (ex.: consumíveis, material). 
    private Integer quantidadeProdutos;

    // Quantidade de peças de roupa utilizadas/consumidas. 
    private Integer quantidadeRoupa;

    // Campo livre para observações/contexto sobre o consumo. 
    private String observacoesConsumo;

    // Identificador do funcionário que registou/confirmou o consumo.
    private Long funcionarioId;

    // ================= Getters e Setters =================

    // return quantidade de produtos consumidos.
    public Integer getQuantidadeProdutos() { return quantidadeProdutos; }

    // Define a quantidade de produtos consumidos.
    public void setQuantidadeProdutos(Integer quantidadeProdutos) { this.quantidadeProdutos = quantidadeProdutos; }

    // return quantidade de peças de roupa consumidas. 
    public Integer getQuantidadeRoupa() { return quantidadeRoupa; }

    // Define a quantidade de peças de roupa consumidas.
    public void setQuantidadeRoupa(Integer quantidadeRoupa) { this.quantidadeRoupa = quantidadeRoupa; }

    // return observações/contexto do consumo.
    public String getObservacoesConsumo() { return observacoesConsumo; }

    // Define observações/contexto do consumo.
    public void setObservacoesConsumo(String observacoesConsumo) { this.observacoesConsumo = observacoesConsumo; }

    // @return ID do funcionário que registou o consumo.
    public Long getFuncionarioId() { return funcionarioId; }

    // Define o ID do funcionário que registou o consumo. 
    public void setFuncionarioId(Long funcionarioId) { this.funcionarioId = funcionarioId; }
}