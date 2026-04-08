package pt.florinhas.marcacoes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoupaDTO {

    private Long id;

    private String categoria;

    private Long itemId;

    private String tamanho;

    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
    private Integer quantidade = 1;
}
