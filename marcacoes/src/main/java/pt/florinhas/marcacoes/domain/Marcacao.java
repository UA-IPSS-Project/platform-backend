package pt.florinhas.marcacoes.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

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

    @Column(name = "data", nullable = false)
    private LocalDateTime data;

    @Column(name = "estado", nullable = false, length = 50)
    private EventoEstado estado;

    // Relacionamento ManyToOne com Funcionario
    @ManyToOne
    @JoinColumn(name = "utilizador_id")
    private Utilizador criadoPor; // Criador da marcação
    
    // Relacionamento OneToOne com Marcacao_Secretaria (relacionamento 1:1)
    @OneToOne(mappedBy = "marcacao", cascade = CascadeType.ALL)
    private MarcacaoSecretaria marcacaoSecretaria;
}