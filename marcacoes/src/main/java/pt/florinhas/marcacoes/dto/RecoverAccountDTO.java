package pt.florinhas.marcacoes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RecoverAccountDTO {
    @NotBlank(message = "O NIF é obrigatório")
    @Pattern(regexp = "\\d{9}", message = "O NIF deve ter 9 dígitos")
    private String nif;

    @Email(message = "Formato de email inválido")
    private String updatedEmail;

    @Pattern(regexp = "\\d{9}", message = "O contacto deve ter 9 dígitos")
    private String updatedContact;
}