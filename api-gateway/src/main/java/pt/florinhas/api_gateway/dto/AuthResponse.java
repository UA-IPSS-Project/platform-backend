package pt.florinhas.api_gateway.dto;

/**
 * DTO imutável (Java record) devolvido após autenticação bem-sucedida.
 *
 * Inclui:
 * - token: credencial (tipicamente JWT) a ser enviada no cabeçalho
 * Authorization.
 * - type: esquema do token no header (por omissão, "Bearer").
 * - id/email/nome/role/nif/telefone: dados públicos do utilizador autenticado.
 * - expiresAt: epoch millis para expiração do token (útil ao frontend para
 * refresh/logout).
 * - active: estado da conta (true se aprovada/ativa).
 */
public record AuthResponse(
        Long id,
        String email,
        String nome,
        String role,
        String nif,
        String telefone,
        long expiresAt,
    boolean active,
    boolean requiresPasswordSetup) {
    /**
     * Construtor principal.
     */
    public AuthResponse(
            Long id,
            String email,
            String nome,
            String role,
            String nif,
            String telefone,
            long expiresAt,
            boolean active,
            boolean requiresPasswordSetup) {
        this.id = id;
        this.email = email;
        this.nome = nome;
        this.role = role;
        this.nif = nif;
        this.telefone = telefone;
        this.expiresAt = expiresAt;
        this.active = active;
        this.requiresPasswordSetup = requiresPasswordSetup;
    }
}
