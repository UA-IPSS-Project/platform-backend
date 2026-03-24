package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarMaterialRequest(
        @NotBlank String nome,
        @NotNull String categoria,
        @NotBlank String atributo,
        @NotBlank String valorAtributo) {
}
