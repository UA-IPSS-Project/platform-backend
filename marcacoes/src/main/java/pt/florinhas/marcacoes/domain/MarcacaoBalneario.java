package pt.florinhas.marcacoes.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA com detalhes específicos de marcações processadas pelo
 * Balneário.
 * Representa serviços de apoio logístico e higiene pessoal.
 */
@Entity
@Table(name = "Marcacao_Balneario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "marcacaoId")
public class MarcacaoBalneario {

    @Id
    @Column(name = "marcacao_id")
    private Long marcacaoId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "marcacao_id")
    private Marcacao marcacao;

    @Column(name = "nome_utente", length = 100, nullable = false)
    private String nomeUtente;

    @Column(name = "produtos_higiene")
    private Boolean produtosHigiene = false;

    @Column(name = "lavagem_ropa")
    private Boolean lavagemRoupa = false;

    /**
     * Responsável pela execução do serviço de balneário.
     */
    @ManyToOne
    @JoinColumn(name = "responsavel_id")
    private Funcionario responsavel;

    /**
     * Peças de roupa fornecidas no balneário.
     */
    @OneToMany(mappedBy = "marcacaoBalneario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Roupa> roupas = new ArrayList<>();

    public void addRoupa(Roupa roupa) {
        roupas.add(roupa);
        roupa.setMarcacaoBalneario(this);
    }

    public void removeRoupa(Roupa roupa) {
        roupas.remove(roupa);
        roupa.setMarcacaoBalneario(null);
    }
}
