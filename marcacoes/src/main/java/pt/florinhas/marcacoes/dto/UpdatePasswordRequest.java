package pt.florinhas.marcacoes.dto;

import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private String newPassword;
    private boolean termsAccepted;
}
