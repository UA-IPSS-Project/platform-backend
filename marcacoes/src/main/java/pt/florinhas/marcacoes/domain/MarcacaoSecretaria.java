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

/**
 * Entidade JPA com detalhes específicos de marcações processadas pela Secretaria.
 *
 * Desenho do mapeamento:
 *  - Relação 1:1 com Marcacao usando PK partilhada (Shared Primary Key).
 *    * O campo marcacaoId é simultaneamente PK desta tabela e FK para Marcacao.
 *    * @MapsId indica que a FK marcacao_id também é a chave primária.
 *  - Campos de contextualização do atendimento (assunto, descrição, tipoAtendimento).
 *  - Associação N:1 ao Utente que é alvo da marcação.
 *
 * Vantagens da PK partilhada:
 *  - Garante que existe no máximo um registo de MarcacaoSecretaria por Marcacao.
 *  - Evita chaves artificiais e simplifica joins.
 */
@Entity
@Table(name = "Marcacao_Secretaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "marcacaoId")
public class MarcacaoSecretaria {

    /**
     * Chave primária desta entidade e, simultaneamente, FK para Marcacao.
     * É preenchida a partir da associação @OneToOne com @MapsId.
     */
	@Id
    @Column(name = "marcacao_id")
    private Long marcacaoId;

    /**
     * Associação 1:1 para a entidade Marcacao.
     *
     * MapsId indica que a PK desta entidade é derivada da PK de Marcacao,
     * tornando 'marcacao_id' a coluna de ligação e a chave primária.
     */
    @OneToOne
    @MapsId // Indica que marcacao_id é simultaneamente PK e FK
    @JoinColumn(name = "marcacao_id")
    private Marcacao marcacao;

    /**
     * Assunto sintetizado do atendimento (curto).
     * Comprimento máximo: 100 caracteres.
     */
    @Column(name = "assunto", length = 100)
    private String assunto;

    /**
     * Descrição detalhada do pedido/atendimento.
     * Comprimento máximo: 300 caracteres.
     */
    @Column(name = "descricao", length = 300)
    private String descricao;

    /**
     * Tipo de atendimento (PRESENCIAL, REMOTO).
     * Persistido como string para legibilidade e robustez a reordenações do enum.
     */
	@Enumerated(EnumType.STRING)
	private AtendimentoTipo tipoAtendimento; // PRESENCIAL, REMOTO

    /**
     * Utente associado à marcação.
     * Relação N:1 — vários registos MarcacaoSecretaria podem referir o mesmo utente.
     * A FK é armazenada na coluna 'utente_id'.
     */
	@ManyToOne
	@JoinColumn(name = "utente_id")
	private Utente utente; // Utente associado à marcação
}
