package pt.florinhas.api_gateway.dto;

import jakarta.validation.constraints.NotBlank;

public record RecoverPasswordRequest(
    @NotBlank(message = "Identificador é obrigatório (email ou NIF)")
    String identifier
) {}
