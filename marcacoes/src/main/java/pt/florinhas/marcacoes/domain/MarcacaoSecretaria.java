package pt.florinhas.marcacoes.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "marcacao_secretaria")
public class MarcacaoSecretaria extends Marcacao {

	private String assunto;

	public MarcacaoSecretaria() {
	}

	public String getAssunto() {
		return assunto;
	}

	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}
}
