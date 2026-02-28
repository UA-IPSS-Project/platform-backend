package pt.florinhas.marcacoes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidade JPA que representa uma peça de roupa no contexto do Balneário.
 */
@Entity
@Table(name = "Roupa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Roupa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "categoria", length = 50, nullable = false)
    private String categoria;

    @Column(name = "tamanho", length = 20)
    private String tamanho;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade = 1;

    @ManyToOne
    @JoinColumn(name = "marcacao_balneario_id", nullable = false)
    private MarcacaoBalneario marcacaoBalneario;
}
