package pt.florinhas.marcacoes.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

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

	@Enumerated(EnumType.STRING)
	private AtendimentoTipo tipoAtendimento; // PRESENCIAL, REMOTO
	
	@ManyToOne
	@JoinColumn(name = "utente_id")
	private Utente utente;          // Utente associado à marcaçã
	
}
