package pt.florinhas.requisicoes.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

public record CriarRequisicaoTransporteRequest(
        String descricao,
        @NotNull RequisicaoPrioridade prioridade,
        @NotNull Long criadoPorId,
        Long geridoPorId,
        String destino,
        @NotNull LocalDateTime dataHoraSaida,
        @NotNull LocalDateTime dataHoraRegresso,
        @NotNull @PositiveOrZero Integer numeroPassageiros,
        String condutor,
        List<Long> transporteIds,
        @Deprecated
        Long transporteId) {
}
