package pt.florinhas.api_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private Long id;
    private String email;
    private String nome;
    private String role;
    private String nif;
    private String telefone;
    private long expiresAt;
    private boolean active;
    private boolean requiresPasswordSetup;
}
