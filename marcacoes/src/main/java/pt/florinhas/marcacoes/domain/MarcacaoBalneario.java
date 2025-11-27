package pt.florinhas.marcacoes.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "marcacao_balneario")
public class MarcacaoBalneario extends Marcacao {

	private Boolean produtosHigiene;
	private Boolean lavagemRoupa;
	private String roupaDescricao;
	
	private Integer quantidadeProdutos;
	private Integer quantidadeRoupa;
	private String observacoesConsumo;
	private Boolean consumoRegistado;

	public MarcacaoBalneario() {
		super();
	}

	public Boolean getProdutosHigiene() { return produtosHigiene; }
	public void setProdutosHigiene(Boolean produtosHigiene) { this.produtosHigiene = produtosHigiene; }

	public Boolean getLavagemRoupa() { return lavagemRoupa; }
	public void setLavagemRoupa(Boolean lavagemRoupa) { this.lavagemRoupa = lavagemRoupa; }

	public String getRoupaDescricao() { return roupaDescricao; }
	public void setRoupaDescricao(String roupaDescricao) { this.roupaDescricao = roupaDescricao; }
	
	public Integer getQuantidadeProdutos() { return quantidadeProdutos; }
	public void setQuantidadeProdutos(Integer quantidadeProdutos) { this.quantidadeProdutos = quantidadeProdutos; }
	
	public Integer getQuantidadeRoupa() { return quantidadeRoupa; }
	public void setQuantidadeRoupa(Integer quantidadeRoupa) { this.quantidadeRoupa = quantidadeRoupa; }
	
	public String getObservacoesConsumo() { return observacoesConsumo; }
	public void setObservacoesConsumo(String observacoesConsumo) { this.observacoesConsumo = observacoesConsumo; }
	
	public Boolean getConsumoRegistado() { return consumoRegistado; }
	public void setConsumoRegistado(Boolean consumoRegistado) { this.consumoRegistado = consumoRegistado; }
}