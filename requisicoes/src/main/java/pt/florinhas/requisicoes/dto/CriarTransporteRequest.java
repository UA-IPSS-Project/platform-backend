package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.requisicoes.domain.TransporteCategoria;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarTransporteRequest {
    private String codigo;
    @NotBlank
    private String tipo;
    @NotNull
    private TransporteCategoria categoria;
    @NotBlank
    private String matricula;
    private String marca;
    private String modelo;
    private Integer lotacao;
    private LocalDate dataMatricula;
}
