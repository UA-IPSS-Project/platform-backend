package pt.florinhas.marcacoes.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "relatorio_periodico")
@Data
@NoArgsConstructor
public class RelatorioPeriodico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String destinatarios; // emails separados por vírgula

    @Column(nullable = false, length = 20)
    private String frequencia; // DIARIO, SEMANAL, MENSAL

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private String seccoes; // secções do relatório separadas por vírgula

    @Column(name = "activo")
    private boolean activo = true;
}
