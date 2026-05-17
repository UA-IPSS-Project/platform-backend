package pt.florinhas.requisicoes.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Transporte", indexes = {
    @Index(name = "idx_transporte_categoria", columnList = "categoria")
})
@Data
@NoArgsConstructor
public class Transporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, unique = true)
    private String codigo;

    @Column(length = 80)
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private TransporteCategoria categoria;

    @Column(length = 20, unique = true)
    private String matricula;

    @Column(length = 80)
    private String marca;

    @Column(length = 80)
    private String modelo;

    private Integer lotacao;

    private LocalDate dataMatricula;
}
