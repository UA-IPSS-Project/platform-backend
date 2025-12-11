package pt.florinhas.marcacoes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.marcacoes.domain.EventoEstado;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarEstadoRequest {
    private String novoEstado;
    private Long funcionarioId;
    private Long version;
    
    public EventoEstado getNovoEstadoEnum() {
        return EventoEstado.valueOf(novoEstado);
    }
}