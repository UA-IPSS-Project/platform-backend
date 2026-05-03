package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManutencaoItemRequest {
    @NotNull
    private Long itemId;
    private Long transporteId;
    private String observacoes;
}
