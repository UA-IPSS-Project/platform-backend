package pt.florinhas.marcacoes.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificarDocumentosRequest {
    private String observacoes;
    private Long funcionarioId;
}