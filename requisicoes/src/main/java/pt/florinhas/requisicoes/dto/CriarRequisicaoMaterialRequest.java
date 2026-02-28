package pt.florinhas.requisicoes.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

public record CriarRequisicaoMaterialRequest(
        @NotBlank String descricao,
        @NotNull RequisicaoPrioridade prioridade,
        LocalDateTime tempoLimite,
        @NotNull Long criadoPorId,
        Long geridoPorId,
        @NotNull Long materialId,
        @NotNull @Min(1) Integer quantidade) {
}
