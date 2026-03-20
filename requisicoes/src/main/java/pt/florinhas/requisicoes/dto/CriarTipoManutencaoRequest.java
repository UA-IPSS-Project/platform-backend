package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarTipoManutencaoRequest(
        @NotBlank String nome,
        String descricao) {
}
