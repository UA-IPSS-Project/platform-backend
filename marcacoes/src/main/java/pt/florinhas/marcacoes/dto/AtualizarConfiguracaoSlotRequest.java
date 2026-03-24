package pt.florinhas.marcacoes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarConfiguracaoSlotRequest {

    @NotNull(message = "A capacidade por slot é obrigatória")
    @Min(value = 1, message = "A capacidade por slot deve ser pelo menos 1")
    @Max(value = 20, message = "A capacidade por slot não pode exceder 20")
    private Integer capacidadePorSlot;
}