package pt.florinhas.marcacoes.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FuncionarioRegisterRequest(
    @NotBlank(message = "Nome é obrigatório")
    String nome,
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    String email,
    
    @NotBlank(message = "Password é obrigatória")
    @Size(min = 6, message = "Password deve ter pelo menos 6 caracteres")
    String password,
    
    @NotBlank(message = "NIF é obrigatório")
    @Size(min = 9, max = 9, message = "NIF deve ter 9 dígitos")
    String nif,
    
    @NotBlank(message = "Contacto é obrigatório")
    String contacto,
    
    @NotBlank(message = "Função é obrigatória")
    String funcao,
    
    @NotNull(message = "Data de nascimento é obrigatória")
    LocalDate dataNasc
) {}
