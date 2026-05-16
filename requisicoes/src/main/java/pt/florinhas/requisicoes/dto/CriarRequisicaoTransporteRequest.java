package pt.florinhas.requisicoes.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

public record CriarRequisicaoTransporteRequest(
        String descricao,
        @NotNull RequisicaoPrioridade prioridade,
        Long geridoPorId,
        String destino,
        @NotNull LocalDateTime dataHoraSaida,
        @NotNull LocalDateTime dataHoraRegresso,
        @NotNull @PositiveOrZero Integer numeroPassageiros,
        @NotBlank String condutor,
        @NotEmpty List<Long> transporteIds,
        @jakarta.validation.Valid RequisicaoPeriodicaConfigRequest periodica) {
}
