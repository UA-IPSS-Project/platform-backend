package pt.florinhas.requisicoes.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Requisicao_Manutencao")
@PrimaryKeyJoinColumn(name = "requisicao_id")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RequisicaoManutencao extends Requisicao {

    @Column(length = 200)
    private String assunto;

    @OneToMany(mappedBy = "requisicao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequisicaoManutencaoItem> itens = new ArrayList<>();
}
