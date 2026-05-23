package pt.florinhas.marcacoes.domain;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

import pt.florinhas.common_data.domain.Utilizador;

/**
 * Entidade JPA que representa um bloqueio de agenda.
 *
 * Um bloqueio define um intervalo temporal (numa data específica) durante o
 * qual
 * não é possível criar marcações. É tipicamente criado por um funcionário/admin
 * para assinalar indisponibilidade (reuniões internas, manutenção, feriados,
 * etc.).
 */
@Entity
@Table(name = "bloqueios_agenda", indexes = {
    @Index(name = "idx_bloqueio_data_tipo", columnList = "data, tipo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloqueioAgenda {

    /**
     * Chave primária autogerada (IDENTITY).
     * Ideal para cenários em que a base de dados gere o ID incrementalmente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Data civil do bloqueio (YYYY-MM-DD).
     * É obrigatória e define o dia a que o bloqueio pertence.
     */
    @Column(nullable = false)
    private LocalDate data;

    /**
     * Hora local de início do bloqueio (HH:mm[:ss]).
     * Obrigatória. Deve ser anterior a horaFim.
     */
    @Column(nullable = false)
    private LocalTime horaInicio;

    /**
     * Hora local de fim do bloqueio (HH:mm[:ss]).
     * Obrigatória. Deve ser posterior a horaInicio.
     */
    @Column(nullable = false)
    private LocalTime horaFim;

    // Texto livre a justificar o bloqueio (ex.: "Reunião interna", "Feriado").
    private String motivo;

    /**
     * Tipo de agenda a que o bloqueio pertence: SECRETARIA ou BALNEARIO.
     * Permite que cada departamento gere os seus bloqueios de forma independente.
     */
    @Column(nullable = false, columnDefinition = "varchar(255) default 'SECRETARIA'")
    @Builder.Default
    private String tipo = "SECRETARIA";

    /**
     * Utilizador (tipicamente funcionário/admin) que criou o bloqueio.
     * Relação N:1 — vários bloqueios podem ser criados pelo mesmo utilizador.
     *
     * A FK é armazenada na coluna 'bloqueado_por_id'.
     * Caso o seu modelo permita bloqueios sem autor explícito, esta relação pode
     * ser opcional.
     */
    @ManyToOne
    @JoinColumn(name = "bloqueado_por_id")
    private Utilizador bloqueadoPor;
}
