package pt.florinhas.requisicoes.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

public record CriarRequisicaoManutencaoRequest(
        String descricao,
        @NotNull RequisicaoPrioridade prioridade,
        Long geridoPorId,
        @NotNull @NotEmpty List<@Valid ManutencaoItemRequest> manutencaoItens,
        RequisicaoPeriodicaConfigRequest periodica) {
}
