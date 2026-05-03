package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarEstadoRequisicaoRequest {
    @NotNull
    private RequisicaoEstado estado;
}
