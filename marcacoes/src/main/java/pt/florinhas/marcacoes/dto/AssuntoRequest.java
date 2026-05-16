package pt.florinhas.marcacoes.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para criação e atualização do nome de um assunto.
 */
public record AssuntoRequest(
    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome não pode exceder 100 caracteres")
    String nome
) {}
