package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarMaterialRequest(
        @NotBlank String nome,
        String descricao) {
}
