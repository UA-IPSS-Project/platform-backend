package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotNull;
import pt.florinhas.requisicoes.domain.TransporteCategoria;

/**
 * DTO para atualizar a categoria de um transporte.
 * Especialmente utilizado para transições para estado ABATE_VENDIDO.
 */
public record AtualizarCategoriaTransporteRequest(
        @NotNull(message = "A categoria do transporte é obrigatória.")
        TransporteCategoria categoria
) {
}
