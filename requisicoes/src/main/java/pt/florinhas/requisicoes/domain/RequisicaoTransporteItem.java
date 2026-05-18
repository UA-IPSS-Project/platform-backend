package pt.florinhas.requisicoes.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Table(name = "Requisicao_Transporte_Item", indexes = {
    @Index(name = "idx_req_tra_item_req_id",  columnList = "requisicao_id"),
    @Index(name = "idx_req_tra_item_tra_id",  columnList = "transporte_id")
})
@Data
@NoArgsConstructor
public class RequisicaoTransporteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transporte_id", nullable = false)
    private Transporte transporte;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "requisicao_id", nullable = false)
    private RequisicaoTransporte requisicao;
}