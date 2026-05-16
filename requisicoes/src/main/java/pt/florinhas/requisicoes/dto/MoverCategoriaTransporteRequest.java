package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotNull;
import pt.florinhas.requisicoes.domain.TransporteCategoria;

/**
 * DTO para mover todos os veículos de uma categoria para outra.
 * Utilizado quando uma categoria é eliminada - todos os veículos são movidos para ABATIDO_VENDIDO_DESCONTINUADO.
 */
public record MoverCategoriaTransporteRequest(
        @NotNull(message = "A categoria de origem é obrigatória.")
        TransporteCategoria origem,
        
        @NotNull(message = "A categoria de destino é obrigatória.")
        TransporteCategoria destino
) {
}