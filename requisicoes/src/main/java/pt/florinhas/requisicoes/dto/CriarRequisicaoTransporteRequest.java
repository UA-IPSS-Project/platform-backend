package pt.florinhas.requisicoes.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

public record CriarRequisicaoTransporteRequest(
        @NotBlank String descricao,
        @NotNull RequisicaoPrioridade prioridade,
        LocalDateTime tempoLimite,
        @NotNull Long criadoPorId,
        Long geridoPorId,
        @NotNull Long transporteId) {
}
