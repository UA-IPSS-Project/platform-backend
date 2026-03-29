package pt.florinhas.requisicoes.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

public record CriarRequisicaoMaterialRequest(
        String descricao,
        @NotNull RequisicaoPrioridade prioridade,
        @NotNull Long criadoPorId,
        Long geridoPorId,
        @NotEmpty List<@Valid ItemMaterialRequest> itens) {

    public record ItemMaterialRequest(
            @NotNull Long materialId,
            @NotNull @Min(1) Integer quantidade) {
    }
}
