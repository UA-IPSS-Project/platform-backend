package pt.florinhas.marcacoes.dto;

public record AuthResponse(
        String token,
        String type,
        Long id,
        String email,
        String nome,
        String role,
        String nif,
        String telefone,
        long expiresAt,
        boolean active) {
    public AuthResponse(String token, Long id, String email, String nome, String role, String nif, String telefone,
            long expiresAt, boolean active) {
        this(token, "Bearer", id, email, nome, role, nif, telefone, expiresAt, active);
    }
}
