package pt.florinhas.requisicoes.domain;

import pt.florinhas.common_data.domain.Funcionario;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "Requisicao")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
public abstract class Requisicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RequisicaoEstado estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RequisicaoPrioridade prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RequisicaoTipo tipo;

    @ManyToOne
    @JoinColumn(name = "criado_por_id", nullable = false)
    private Funcionario criadoPor;

    @ManyToOne
    @JoinColumn(name = "gerido_por_id")
    private Funcionario geridoPor;

    @Column(name = "criado_em", updatable = false, nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "ultima_alteracao_estado_em", nullable = false)
    private LocalDateTime ultimaAlteracaoEstadoEm;

    @PrePersist
    protected void onCreate() {
        if (estado == null) {
            estado = RequisicaoEstado.ABERTO;
        }
        criadoEm = LocalDateTime.now();
        ultimaAlteracaoEstadoEm = criadoEm;
    }
}
