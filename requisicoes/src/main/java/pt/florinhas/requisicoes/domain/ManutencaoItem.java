package pt.florinhas.requisicoes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Manutencao_Item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManutencaoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String categoria;

    @Column(nullable = false, length = 120)
    private String espaco;

    @Column(nullable = false, length = 120)
    private String itemVerificacao;
}
