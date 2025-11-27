package pt.florinhas.marcacoes.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "utente")
public class Utente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nome;
	private String nif;
	private String telefone;
	private String telefoneAlternativo;
	private String email;
	private LocalDate dataNascimento;
	
	private Boolean contaCriadaAutomaticamente;
	private Boolean passwordDefinida;

	@OneToMany(mappedBy = "utente")
	private List<Marcacao> marcacoes = new ArrayList<>();

	public Utente() {
		this.contaCriadaAutomaticamente = false;
		this.passwordDefinida = false;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getNome() { return nome; }
	public void setNome(String nome) { this.nome = nome; }

	public String getNif() { return nif; }
	public void setNif(String nif) { this.nif = nif; }

	public String getTelefone() { return telefone; }
	public void setTelefone(String telefone) { this.telefone = telefone; }
	
	public String getTelefoneAlternativo() { return telefoneAlternativo; }
	public void setTelefoneAlternativo(String telefoneAlternativo) { this.telefoneAlternativo = telefoneAlternativo; }
	
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public LocalDate getDataNascimento() { return dataNascimento; }
	public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
	
	public Boolean getContaCriadaAutomaticamente() { return contaCriadaAutomaticamente; }
	public void setContaCriadaAutomaticamente(Boolean contaCriadaAutomaticamente) { this.contaCriadaAutomaticamente = contaCriadaAutomaticamente; }
	
	public Boolean getPasswordDefinida() { return passwordDefinida; }
	public void setPasswordDefinida(Boolean passwordDefinida) { this.passwordDefinida = passwordDefinida; }

	public List<Marcacao> getMarcacoes() { return marcacoes; }
	public void setMarcacoes(List<Marcacao> marcacoes) { this.marcacoes = marcacoes; }
}