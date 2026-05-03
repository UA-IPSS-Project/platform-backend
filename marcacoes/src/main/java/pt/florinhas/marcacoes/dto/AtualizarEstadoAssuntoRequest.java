package pt.florinhas.marcacoes.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarEstadoAssuntoRequest {

    @NotNull(message = "O campo ativo é obrigatório")
    private Boolean ativo;
}
