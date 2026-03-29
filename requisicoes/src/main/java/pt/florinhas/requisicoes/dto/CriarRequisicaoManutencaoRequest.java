package pt.florinhas.requisicoes.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

public record CriarRequisicaoManutencaoRequest(
        String descricao,
        @NotNull RequisicaoPrioridade prioridade,
        @NotNull Long criadoPorId,
        Long geridoPorId,
        String assunto,
        List<ManutencaoItemRequest> manutencaoItens) {
}
