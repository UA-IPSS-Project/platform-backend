package pt.florinhas.marcacoes.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;

@Entity
@Table(name = "Utilizador")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Utilizador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Atributos próprios do Utilizador
    @Column(name = "nif", nullable = false, unique = true, length = 9)
    private String nif;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "dataNasc")
    private LocalDate dataNasc;

    @Column(name = "passHash", length = 32)
    private String passHash;

    // Informações de morada
    private String morada;
    private String codigoPostal;
    private String freguesia;

    // Informação profissional
    private String telefoneEmprego;
    private String localEmprego;
    private String moradaEmprego;
    private String profissao;

}