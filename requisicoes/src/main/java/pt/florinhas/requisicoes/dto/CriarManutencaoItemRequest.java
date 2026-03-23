package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarManutencaoItemRequest(
        @NotBlank(message = "Categoria é obrigatória") String categoria,
        @NotBlank(message = "Espaço é obrigatório") String espaco,
        @NotBlank(message = "Item de verificação é obrigatório") String itemVerificacao
) {
}
