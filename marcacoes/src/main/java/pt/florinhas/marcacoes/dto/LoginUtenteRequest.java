package pt.florinhas.marcacoes.dto;

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

    // NIF do utente (exactamente 9 dígitos). 
    @NotBlank(message = "NIF é obrigatório")
    @Pattern(regexp = "^[0-9]{9}$", message = "NIF deve ter 9 dígitos")
    String nif,

    // Palavra-passe em claro recebida do frontend (será verificada no servidor).
    @NotBlank(message = "Password é obrigatória")
    String password
) {}
