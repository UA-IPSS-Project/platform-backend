package pt.florinhas.marcacoes.dto;

public record UserResponse(
    Long id,
    String email,
    String nome,
    String role,
    String nif,
    String telefone
) {
}
