package pt.florinhas.marcacoes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FuncionarioRegisterRequestDTO {
    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O NIF é obrigatório")
    @Pattern(regexp = "\\d{9}", message = "NIF deve conter exatamente 9 dígitos numéricos")
    private String nif;

    private String contacto;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "A função é obrigatória")
    private String funcao;

    @NotBlank(message = "A data de nascimento é obrigatória")
    private String dataNasc;

    @NotBlank(message = "A password é obrigatória")
    private String password;

    private boolean termsAccepted;
}
