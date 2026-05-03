package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarTipoManutencaoCatalogoRequest {
    @NotBlank
    private String nome;
    private String descricao;
}
