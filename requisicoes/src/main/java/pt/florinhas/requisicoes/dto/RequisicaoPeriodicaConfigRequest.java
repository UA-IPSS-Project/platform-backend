package pt.florinhas.requisicoes.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import pt.florinhas.requisicoes.domain.PeriodicidadeFrequencia;

public record RequisicaoPeriodicaConfigRequest(
        @NotNull PeriodicidadeFrequencia frequencia,
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate dataFim) {

    @AssertTrue(message = "dataInicio must be before or equal to dataFim")
    public boolean isIntervaloDatasValido() {
        return dataInicio == null || dataFim == null || !dataInicio.isAfter(dataFim);
    }
}
