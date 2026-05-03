package pt.florinhas.requisicoes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarRequisicaoTransporteRequest {
    private String descricao;
    @NotNull
    private RequisicaoPrioridade prioridade;
    private Long geridoPorId;
    private String destino;
    @NotNull
    private LocalDateTime dataHoraSaida;
    @NotNull
    private LocalDateTime dataHoraRegresso;
    @NotNull
    @PositiveOrZero
    private Integer numeroPassageiros;
    @NotBlank
    private String condutor;
    @NotEmpty
    private List<Long> transporteIds;
    @Deprecated
    private Long transporteId;
}
