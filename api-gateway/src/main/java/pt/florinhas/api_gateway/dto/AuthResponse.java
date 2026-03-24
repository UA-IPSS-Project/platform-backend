package pt.florinhas.api_gateway.dto;

public record AuthResponse(
        Long id,
        String email,
        String nome,
        String role,
        String nif,
        String telefone,
        long expiresAt,
        boolean active) {
}
