package pt.florinhas.common_data.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;
import pt.florinhas.common_data.domain.NotificacaoTipo;

@Data
public class NotificacaoResponseDTO {
    private Long id;
    private String titulo;
    private String mensagem;
    private NotificacaoTipo tipo;
    private boolean lida;
    private LocalDateTime dataCriacao;
    private Long utilizadorId;
    private Map<String, Object> metadata;
}
