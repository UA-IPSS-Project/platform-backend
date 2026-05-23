package pt.florinhas.requisicoes.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Requisicao_Manutencao_Item", indexes = {
    @Index(name = "idx_req_man_item_req_id",  columnList = "requisicao_id"),
    @Index(name = "idx_req_man_item_man_id",  columnList = "manutencao_item_id")
})
@Data
@NoArgsConstructor
public class RequisicaoManutencaoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "manutencao_item_id", nullable = false)
    private ManutencaoItem manutencaoItem;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "requisicao_id", nullable = false)
    private RequisicaoManutencao requisicao;

    @ManyToOne
    @JoinColumn(name = "transporte_id")
    private Transporte transporte;

    @Column(length = 255)
    private String observacoes;
}
