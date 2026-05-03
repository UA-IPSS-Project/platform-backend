package pt.florinhas.common_data.dto;

/**
 * DTO imutável (Java record) com os dados públicos de um utilizador autenticado.
 *
 * Usos típicos:
 *  - Responder ao endpoint /api/auth/me
 *  - Preencher o contexto de sessão no frontend (nome, role, contacto)
 */
public record UserResponse(
    Long id,
    String email,
    String nome,
    String role,
    String nif,
    String telefone
) { }
