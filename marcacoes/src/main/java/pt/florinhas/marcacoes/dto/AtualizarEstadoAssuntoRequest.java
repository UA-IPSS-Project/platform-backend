package pt.florinhas.marcacoes.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para atualização parcial do estado de um assunto.
 */
public record AtualizarEstadoAssuntoRequest(
    @NotNull(message = "O campo ativo é obrigatório")
    Boolean ativo
) {}
