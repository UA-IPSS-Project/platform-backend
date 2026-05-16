package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotNull;

public record ManutencaoItemRequest(
        @NotNull Long itemId,
        Long transporteId,
        String observacoes) {
}