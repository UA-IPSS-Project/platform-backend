package pt.florinhas.requisicoes.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pt.florinhas.requisicoes.domain.TransporteCategoria;

public record CriarTransporteRequest(
        String codigo,
        @NotBlank String tipo,
        @NotNull TransporteCategoria categoria,
        @NotBlank String matricula,
        String marca,
        String modelo,
        Integer lotacao,
        LocalDate dataMatricula) {
}
