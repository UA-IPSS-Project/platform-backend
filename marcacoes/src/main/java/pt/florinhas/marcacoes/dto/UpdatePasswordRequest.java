package pt.florinhas.marcacoes.dto;

public record UpdatePasswordRequest(String newPassword, Boolean termsAccepted) {
}
