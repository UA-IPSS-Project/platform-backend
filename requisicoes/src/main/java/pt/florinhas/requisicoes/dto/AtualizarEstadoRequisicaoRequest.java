package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotNull;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;

public record AtualizarEstadoRequisicaoRequest(
        @NotNull RequisicaoEstado estado) {
}
