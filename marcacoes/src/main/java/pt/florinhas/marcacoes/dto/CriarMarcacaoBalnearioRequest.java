package pt.florinhas.marcacoes.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarMarcacaoBalnearioRequest {

    @NotNull(message = "A data da marcação é obrigatória")
    private LocalDateTime data;

    private String nomeUtente;

    private Boolean produtosHigiene = false;
    private Boolean lavagemRoupa = false;

    private Long responsavelId;

    private List<RoupaDTO> roupas = new ArrayList<>();

    private String observacoes;

    private Long reservaId;
}
