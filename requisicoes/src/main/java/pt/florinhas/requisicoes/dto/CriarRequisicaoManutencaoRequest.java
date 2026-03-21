package pt.florinhas.requisicoes.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

public record CriarRequisicaoManutencaoRequest(
        @NotBlank String descricao,
        @NotNull RequisicaoPrioridade prioridade,
        LocalDateTime tempoLimite,
        @NotNull Long criadoPorId,
        Long geridoPorId,
        String assunto,
        List<Long> manutencaoItemIds) {
}
