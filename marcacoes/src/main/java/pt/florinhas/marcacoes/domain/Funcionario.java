package pt.florinhas.marcacoes.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "funcionario")
public class Funcionario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String nome;
	private String nif;
	private String email;
	private String tipo; // SECRETARIA, BALNEARIO

	@OneToMany(mappedBy = "funcionario")
	private List<Marcacao> marcacoes = new ArrayList<>();

	public Funcionario() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getNif() {
		return nif;
	}

	public void setNif(String nif) {
		this.nif = nif;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) { this.email = email; }

	public List<Marcacao> getMarcacoes() { return marcacoes; }

	public void setMarcacoes(List<Marcacao> marcacoes) { this.marcacoes = marcacoes; }

	public String getTipo() { return tipo; }
	public void setTipo(String tipo) { this.tipo = tipo; }
}
