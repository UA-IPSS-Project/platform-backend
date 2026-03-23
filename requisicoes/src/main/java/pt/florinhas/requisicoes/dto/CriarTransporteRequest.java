package pt.florinhas.requisicoes.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarTransporteRequest(
        String codigo,
        @NotBlank String tipo,
        @NotNull String categoria,
        @NotBlank String matricula,
        String marca,
        String modelo,
        Integer lotacao,
        LocalDate dataMatricula) {
}
