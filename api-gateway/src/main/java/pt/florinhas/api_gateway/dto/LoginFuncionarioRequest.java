package pt.florinhas.api_gateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Java record) para pedido de login de Funcionário.
 *
 * Integra Bean Validation para validação automática em controllers com @Valid:
 *  - email: obrigatório e com formato válido
 *  - password: obrigatória (conteúdo validado na camada de serviço)
 */
public record LoginFuncionarioRequest(

    // Email institucional/pessoal do funcionário. 
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,

    // Palavra-passe em claro recebida do frontend (será cifrada/validada no servidor). 
    @NotBlank(message = "Password é obrigatória")
    String password
) {}
