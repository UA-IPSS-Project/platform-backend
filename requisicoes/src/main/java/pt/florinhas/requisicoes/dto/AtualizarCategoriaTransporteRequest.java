package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.requisicoes.domain.TransporteCategoria;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarCategoriaTransporteRequest {
    @NotNull(message = "A categoria do transporte é obrigatória.")
    private TransporteCategoria categoria;
}
