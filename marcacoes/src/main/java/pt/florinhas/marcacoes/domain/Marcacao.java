package pt.florinhas.marcacoes.domain;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Marcacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Marcacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "data", nullable = false)
    private LocalDateTime data;

    @Column(name = "estado", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EventoEstado estado;

    // Relacionamento ManyToOne com Funcionario
    @ManyToOne
    @JoinColumn(name = "utilizador_id")
    private Utilizador criadoPor; // Criador da marcação

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    // Funcionário que atendeu/concluiu a marcação
    @ManyToOne
    @JoinColumn(name = "atendente_id")
    private Utilizador atendente;

    // Relacionamento OneToOne com Marcacao_Secretaria (relacionamento 1:1)
    @OneToOne(mappedBy = "marcacao", cascade = CascadeType.ALL)
    private MarcacaoSecretaria marcacaoSecretaria;
}