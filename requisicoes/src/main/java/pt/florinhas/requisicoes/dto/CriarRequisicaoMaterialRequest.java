package pt.florinhas.requisicoes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarRequisicaoMaterialRequest {
    private String descricao;
    @NotNull
    private RequisicaoPrioridade prioridade;
    private Long geridoPorId;
    @NotEmpty
    private List<@Valid ItemMaterialRequest> itens;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemMaterialRequest {
        @NotNull
        private Long materialId;
        @NotNull
        @Min(1)
        private Integer quantidade;
    }
}
