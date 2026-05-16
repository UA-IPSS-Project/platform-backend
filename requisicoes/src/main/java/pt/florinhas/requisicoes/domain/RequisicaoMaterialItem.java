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
@Table(name = "Requisicao_Material_Item")
@Data
@NoArgsConstructor
public class RequisicaoMaterialItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    private Integer quantidade;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "requisicao_id", nullable = false)
    private RequisicaoMaterial requisicao;
}