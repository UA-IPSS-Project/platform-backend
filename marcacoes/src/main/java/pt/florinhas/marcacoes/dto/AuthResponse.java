package pt.florinhas.marcacoes.dto;

public record AuthResponse(
    String token,
    String type,
    Long id,
    String email,
    String nome,
    String role
) {
    public AuthResponse(String token, Long id, String email, String nome, String role) {
        this(token, "Bearer", id, email, nome, role);
    }
}
