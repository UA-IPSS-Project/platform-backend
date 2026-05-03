package pt.florinhas.api_gateway.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuncionarioRegisterRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;

    @NotBlank(message = "Password é obrigatória")
    @Size(min = 6, message = "Password deve ter pelo menos 6 caracteres")
    private String password;

    @NotBlank(message = "NIF é obrigatório")
    @Pattern(regexp = "\\d{9}", message = "NIF deve conter exatamente 9 dígitos numéricos")
    private String nif;

    @Pattern(regexp = "\\d{9}", message = "Contacto deve ter 9 dígitos")
    private String contacto;

    @NotBlank(message = "Função é obrigatória")
    private String funcao;

    @NotNull(message = "Data de nascimento é obrigatória")
    private LocalDate dataNasc;

    @NotNull(message = "Deve aceitar os termos de uso")
    @AssertTrue(message = "Deve aceitar os termos de uso para se registar")
    private Boolean termsAccepted;
}
