package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarTipoManutencaoCatalogoRequest(
        @NotBlank String nome,
        String descricao) {
}