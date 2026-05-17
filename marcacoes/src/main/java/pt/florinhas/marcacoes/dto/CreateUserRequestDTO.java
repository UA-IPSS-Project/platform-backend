package pt.florinhas.marcacoes.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateUserRequestDTO {
    @NotBlank(message = "O nome é obrigatório")
    private String name;

    @NotBlank(message = "O NIF é obrigatório")
    @Pattern(regexp = "\\d{9}", message = "NIF deve conter exatamente 9 dígitos numéricos")
    private String nif;

    @Pattern(regexp = "\\d{9}", message = "O contacto deve ter 9 dígitos")
    private String contact;

    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "A data de nascimento é obrigatória")
    private String birthDate;

    @JsonProperty("isEmployee")
    @JsonAlias({"employee"})
    private boolean employee;

    private String role;
}