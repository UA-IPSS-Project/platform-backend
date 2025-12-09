package pt.florinhas.marcacoes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Marcacao_Secretaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "marcacaoId")
public class MarcacaoSecretaria {

	@Id
    @Column(name = "marcacao_id")
    private Long marcacaoId;

    @OneToOne
    @MapsId // Indica que Marcacao_id é o ID e a FK
    @JoinColumn(name = "marcacao_id")
    private Marcacao marcacao;
    
    @Column(name = "assunto", length = 100)
    private String assunto;

    @Column(name = "descricao", length = 300)
    private String descricao;


	@Enumerated(EnumType.STRING)
	private AtendimentoTipo tipoAtendimento; // PRESENCIAL, REMOTO
	
	@ManyToOne
	@JoinColumn(name = "utente_id")
	private Utente utente;          // Utente associado à marcaçã
	
}
