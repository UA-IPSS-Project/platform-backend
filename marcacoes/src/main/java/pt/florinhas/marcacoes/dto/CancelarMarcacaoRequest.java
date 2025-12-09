package pt.florinhas.marcacoes.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelarMarcacaoRequest {
    private String motivo;
    private Long funcionarioId;
}