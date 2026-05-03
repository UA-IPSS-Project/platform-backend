package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarMaterialRequest {
    @NotBlank
    private String nome;
    @NotNull
    private String categoria;
    private String atributo;
    private String valorAtributo;
}
