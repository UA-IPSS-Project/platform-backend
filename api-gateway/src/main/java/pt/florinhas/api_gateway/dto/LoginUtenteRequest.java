package pt.florinhas.api_gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUtenteRequest {

    @NotBlank(message = "NIF é obrigatório")
    @Pattern(regexp = "^[0-9]{9}$", message = "NIF deve ter 9 dígitos")
    private String nif;

    @NotBlank(message = "Password é obrigatória")
    private String password;
}
