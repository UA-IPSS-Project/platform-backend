package pt.florinhas.requisicoes.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Requisicao_Transporte_Item")
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