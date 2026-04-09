package pt.florinhas.marcacoes.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidade JPA que representa uma Marcação (evento no calendário).
 */
@Entity
@Table(name = "Marcacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Marcacao {

    /**
     * Chave primária autogerada (IDENTITY).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Campo de versão para concorrência otimista.
     * É incrementado automaticamente pelo JPA em cada update e usado no WHERE,
     * lançando OptimisticLockException quando há conflito de escrita.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Data/hora agendada para a marcação (instante do atendimento).
     * Obrigatório.
     */
    @Column(name = "data", nullable = false)
    private LocalDateTime data;

    /**
     * Duração da marcação em minutos.
     * Modular para cada tipo.
     */
    @Column(name = "duration", nullable = false)
    private Integer duration;

    /**
     * Estado do ciclo de vida da marcação (AGENDADO, EM_PROGRESSO, ...).
     * Persistido como texto (STRING) para legibilidade e robustez a reordenações.
     */
    @Column(name = "estado", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EventoEstado estado;

    /**
     * Utilizador que criou a marcação (ex.: funcionário de secretaria ou o próprio
     * utente).
     * Relação N:1. A FK é armazenada em 'utilizador_id'.
     */
    // Relacionamento ManyToOne com Funcionario
    @ManyToOne
    @JoinColumn(name = "utilizador_id")
    private Utilizador criadoPor; // Criador da marcação

    /**
     * Timestamp de criação do registo. Deve ser preenchido na camada de serviço
     * ou via callback JPA (@PrePersist). Marcado como updatable = false.
     */
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    /**
     * Utilizador que efetivamente atendeu/concluiu a marcação.
     * Pode ser nulo até ao início ou conclusão do atendimento.
     * Relação N:1. A FK é 'atendente_id'.
     */
    // Funcionário que atendeu/concluiu a marcação
    @ManyToOne
    @JoinColumn(name = "atendente_id")
    private Utilizador atendente;

    // Motivo do cancelamento (se estado for CANCELADO)
    @Column(name = "motivo_cancelamento")
    private String motivoCancelamento;

    // Descrição curta da marcação (opcional)
    @Column(name = "descricao")
    private String descricao;

    /**
     * Detalhes específicos quando a marcação segue o fluxo de secretaria.
     * Relação 1:1 bidirecional. 'mappedBy' indica que a FK está do lado de
     * MarcacaoSecretaria.
     * Cascade ALL para propagar persist/update/delete de Marcacao para
     * MarcacaoSecretaria.
     */
    // Relacionamento OneToOne com Marcacao_Secretaria (relacionamento 1:1)
    @OneToOne(mappedBy = "marcacao", cascade = CascadeType.ALL)
    private MarcacaoSecretaria marcacaoSecretaria;

    /**
     * Detalhes específicos quando a marcação segue o fluxo do balneário.
     * Relação 1:1 bidirecional.
     */
    @OneToOne(mappedBy = "marcacao", cascade = CascadeType.ALL)
    private MarcacaoBalneario marcacaoBalneario;

    /**
     * Documentos anexados a esta marcação.
     * Relação 1:N bidirecional. 'mappedBy' indica que a FK está do lado de
     * Documento.
     * Cascade ALL para propagar operações de Marcacao para Documento.
     * orphanRemoval garante que documentos removidos da lista sejam deletados da
     * BD.
     */
    @OneToMany(mappedBy = "marcacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Documento> documentos = new ArrayList<>();

    /**
     * Define a data de criação antes de persistir.
     */
    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }
}
