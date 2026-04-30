package pt.florinhas.requisicoes.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import pt.florinhas.requisicoes.domain.PeriodicidadeFrequencia;

public record RequisicaoPeriodicaConfigRequest(
    @NotNull PeriodicidadeFrequencia frequencia,
    @NotNull LocalDate dataInicio,
    LocalDate dataFim
) {}