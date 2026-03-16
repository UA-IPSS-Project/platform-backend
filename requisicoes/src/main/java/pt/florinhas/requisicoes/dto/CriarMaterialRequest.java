package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pt.florinhas.requisicoes.domain.MaterialCategoria;

public record CriarMaterialRequest(
        @NotBlank String nome,
        @NotNull MaterialCategoria categoria,
        @NotBlank String atributo,
        @NotBlank String valorAtributo) {
}
