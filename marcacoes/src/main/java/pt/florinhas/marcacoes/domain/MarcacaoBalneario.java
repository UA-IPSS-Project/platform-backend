package pt.florinhas.marcacoes.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "marcacao_balneario")
public class MarcacaoBalneario extends Marcacao {

	private Boolean produtosHigiene;

	private Boolean lavagemRoupa;

	private String roupaDescricao;

	public MarcacaoBalneario() {
	}

	public Boolean getProdutosHigiene() {
		return produtosHigiene;
	}

	public void setProdutosHigiene(Boolean produtosHigiene) {
		this.produtosHigiene = produtosHigiene;
	}

	public Boolean getLavagemRoupa() {
		return lavagemRoupa;
	}

	public void setLavagemRoupa(Boolean lavagemRoupa) {
		this.lavagemRoupa = lavagemRoupa;
	}

	public String getRoupaDescricao() {
		return roupaDescricao;
	}

	public void setRoupaDescricao(String roupaDescricao) {
		this.roupaDescricao = roupaDescricao;
	}
}
