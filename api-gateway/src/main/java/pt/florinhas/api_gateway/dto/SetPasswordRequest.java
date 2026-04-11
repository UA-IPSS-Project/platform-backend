package pt.florinhas.api_gateway.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para definir ou redefinir password.
 * Usado quando o utente criado pela secretaria ativa a sua conta pela primeira vez,
 * ou em fluxos de reset de password.
 */
public record SetPasswordRequest(
        
        @NotBlank(message = "Password é obrigatória")
        @Size(min = 6, message = "Password deve ter pelo menos 6 caracteres")
        String password,

        /**
         * Indicador de aceitação dos termos de uso (RGPD).
         * Necessário quando a conta foi criada pela secretaria e ainda não tinha termos aceites.
         */
        @NotNull(message = "Deve aceitar os termos de uso")
        @AssertTrue(message = "Deve aceitar os termos de uso para ativar a conta")
        Boolean termsAccepted
) {
}
