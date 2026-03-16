package pt.florinhas.requisicoes.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

public record CriarRequisicaoTransporteRequest(
        @NotBlank String descricao,
        @NotNull RequisicaoPrioridade prioridade,
        LocalDateTime tempoLimite,
        @NotNull Long criadoPorId,
        Long geridoPorId,
        @NotBlank String destino,
        @NotNull LocalDateTime dataHoraSaida,
        @NotNull LocalDateTime dataHoraRegresso,
        @NotNull @Min(1) Integer numeroPassageiros,
        String condutor,
        @NotEmpty List<@NotNull Long> transporteIds) {
}
