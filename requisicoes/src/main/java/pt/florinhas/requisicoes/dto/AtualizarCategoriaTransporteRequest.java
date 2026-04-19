package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotNull;
import pt.florinhas.requisicoes.domain.TransporteCategoria;

/**
 * DTO para atualizar a categoria de um transporte.
 * Especialmente utilizado para transições para estado ABATIDO_VENDIDO_DESCONTINUADO.
 */
public record AtualizarCategoriaTransporteRequest(
        @NotNull(message = "A categoria do transporte é obrigatória.")
        TransporteCategoria categoria
) {
}
