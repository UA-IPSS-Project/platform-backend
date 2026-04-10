package pt.florinhas.api_gateway.dto;

public record UpdatePasswordRequest(String newPassword, Boolean termsAccepted) {
}
