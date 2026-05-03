package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.requisicoes.domain.TransporteCategoria;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoverCategoriaTransporteRequest {
    @NotNull(message = "A categoria de origem é obrigatória.")
    private TransporteCategoria origem;
    @NotNull(message = "A categoria de destino é obrigatória.")
    private TransporteCategoria destino;
}
