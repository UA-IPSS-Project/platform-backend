package pt.florinhas.marcacoes.dto;

import pt.florinhas.marcacoes.domain.EventoEstado;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarEstadoRequest {
    private EventoEstado novoEstado;
    private Long funcionarioId;
}