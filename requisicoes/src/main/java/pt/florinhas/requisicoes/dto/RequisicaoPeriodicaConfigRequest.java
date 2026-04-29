package pt.florinhas.requisicoes.dto;

import java.time.LocalDate;

import pt.florinhas.requisicoes.domain.PeriodicidadeFrequencia;

public record RequisicaoPeriodicaConfigRequest(
    PeriodicidadeFrequencia frequencia,
    LocalDate dataInicio,
    LocalDate dataFim
) {}