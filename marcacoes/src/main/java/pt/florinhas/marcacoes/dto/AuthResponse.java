package pt.florinhas.marcacoes.dto;

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
    /**
     * Construtor de conveniência que assume o esquema "Bearer" para o tipo de
     * token.
     *
     * param token credencial de acesso (tipicamente JWT)
     * param id identificador do utilizador
     * param email email do utilizador (também usado como username)
     * param nome nome do utilizador
     * param role role/perfil lógico (ex.: "FUNCIONARIO", "UTENTE")
     * param nif NIF do utilizador
     * param telefone telefone do utilizador
     * param expiresAt instante de expiração em epoch millis
     * param active estado de ativação da conta
     */
    public AuthResponse(
            String token,
            Long id,
            String email,
            String nome,
            String role,
            String nif,
            String telefone,
            long expiresAt,
            boolean active) {
        this(token, "Bearer", id, email, nome, role, nif, telefone, expiresAt, active);
    }
}
