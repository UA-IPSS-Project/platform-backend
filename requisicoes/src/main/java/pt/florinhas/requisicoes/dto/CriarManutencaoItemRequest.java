package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarManutencaoItemRequest {
    @NotBlank(message = "Categoria é obrigatória")
    private String categoria;
    @NotBlank(message = "Espaço é obrigatório")
    private String espaco;
    @NotBlank(message = "Item de verificação é obrigatório")
    private String itemVerificacao;
}
