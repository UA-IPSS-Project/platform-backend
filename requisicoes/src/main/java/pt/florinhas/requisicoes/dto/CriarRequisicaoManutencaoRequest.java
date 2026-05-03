package pt.florinhas.requisicoes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarRequisicaoManutencaoRequest {
    private String descricao;
    @NotNull
    private RequisicaoPrioridade prioridade;
    private Long geridoPorId;
    @NotNull
    @NotEmpty
    private List<@Valid ManutencaoItemRequest> manutencaoItens;
}
