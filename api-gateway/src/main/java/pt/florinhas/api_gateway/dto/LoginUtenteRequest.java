package pt.florinhas.api_gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO (Java record) para pedido de login de Utente.
 *
 * Validações:
 *  - nif: obrigatório e composto exatamente por 9 dígitos (regex).
 *  - password: obrigatória (a validação de credenciais faz-se no serviço/Provider).
 */
public record LoginUtenteRequest(

    // Hash SHA-256 do NIF (64 chars hex), calculado pelo frontend.
    @NotBlank(message = "NIF é obrigatório")
    @Pattern(regexp = "^[0-9a-f]{64}$", message = "NIF inválido")
    String nif,

    // Palavra-passe em claro recebida do frontend (será verificada no servidor).
    @NotBlank(message = "Password é obrigatória")
    String password
) {}
