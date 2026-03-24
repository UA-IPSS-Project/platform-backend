package pt.florinhas.marcacoes.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class CreateUserRequestDTO {
    @NotBlank(message = "O nome é obrigatório")
    private String name;

    @NotBlank(message = "O NIF é obrigatório")
    @Pattern(regexp = "\\d{9}", message = "NIF deve conter exatamente 9 dígitos numéricos")
    private String nif;

    @NotBlank(message = "O contacto é obrigatório")
    @Pattern(regexp = "\\d{9}", message = "O contacto deve ter 9 dígitos")
    private String contact;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "A data de nascimento é obrigatória")
    private String birthDate; // YYYY-MM-DD

    @JsonProperty("isEmployee")
    @JsonAlias({ "employee" })
    private boolean employee;
    private String role; // Only if isEmployee is true
}
