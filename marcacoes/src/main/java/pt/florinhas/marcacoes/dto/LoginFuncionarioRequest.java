package pt.florinhas.marcacoes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginFuncionarioRequest(
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,
    
    @NotBlank(message = "Password é obrigatória")
    String password
) {}
