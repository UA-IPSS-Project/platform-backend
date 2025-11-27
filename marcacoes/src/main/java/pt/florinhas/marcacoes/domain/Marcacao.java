package pt.florinhas.marcacoes.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "marcacao")
@Inheritance(strategy = InheritanceType.JOINED)
public class Marcacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate data;
	private LocalTime hora;
	private String estado; // AGENDADO, CONFIRMADO, RECEBIDO, CONCLUIDO, CANCELADO
	private String tipoAtendimento; // PRESENCIAL, REMOTO
	
	private LocalDateTime dataCriacao;
	private LocalDateTime dataAtualizacao;
	
	private Boolean presencaConfirmada;
	private String documentosObservacoes;
	private Boolean documentosInvalidos;

	@ManyToOne
	@JoinColumn(name = "utente_id")
	private Utente utente;

	@ManyToOne
	@JoinColumn(name = "funcionario_id")
	private Funcionario funcionario;

	@ManyToOne
	@JoinColumn(name = "valencia_id")
	private Valencia valencia;
	
	@ManyToOne
	@JoinColumn(name = "criado_por_id")
	private Utilizador criadoPor;

	public Marcacao() {
		this.dataCriacao = LocalDateTime.now();
		this.estado = "AGENDADO";
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public LocalDate getData() { return data; }
	public void setData(LocalDate data) { this.data = data; }

	public LocalTime getHora() { return hora; }
	public void setHora(LocalTime hora) { this.hora = hora; }

	public String getEstado() { return estado; }
	public void setEstado(String estado) { this.estado = estado; }
	
	public String getTipoAtendimento() { return tipoAtendimento; }
	public void setTipoAtendimento(String tipoAtendimento) { this.tipoAtendimento = tipoAtendimento; }
	
	public LocalDateTime getDataCriacao() { return dataCriacao; }
	public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
	
	public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
	public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
	
	public Boolean getPresencaConfirmada() { return presencaConfirmada; }
	public void setPresencaConfirmada(Boolean presencaConfirmada) { this.presencaConfirmada = presencaConfirmada; }
	
	public String getDocumentosObservacoes() { return documentosObservacoes; }
	public void setDocumentosObservacoes(String documentosObservacoes) { this.documentosObservacoes = documentosObservacoes; }
	
	public Boolean getDocumentosInvalidos() { return documentosInvalidos; }
	public void setDocumentosInvalidos(Boolean documentosInvalidos) { this.documentosInvalidos = documentosInvalidos; }

	public Utente getUtente() { return utente; }
	public void setUtente(Utente utente) { this.utente = utente; }

	public Funcionario getFuncionario() { return funcionario; }
	public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }

	public Valencia getValencia() { return valencia; }
	public void setValencia(Valencia valencia) { this.valencia = valencia; }
	
	public Utilizador getCriadoPor() { return criadoPor; }
	public void setCriadoPor(Utilizador criadoPor) { this.criadoPor = criadoPor; }
}