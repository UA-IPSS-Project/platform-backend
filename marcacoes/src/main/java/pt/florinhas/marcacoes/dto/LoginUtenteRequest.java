package pt.florinhas.marcacoes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginUtenteRequest(
    @NotBlank(message = "NIF é obrigatório")
    @Pattern(regexp = "^[0-9]{9}$", message = "NIF deve ter 9 dígitos")
    String nif,
    
    @NotBlank(message = "Password é obrigatória")
    String password
) {}
