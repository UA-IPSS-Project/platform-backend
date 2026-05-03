package pt.florinhas.requisicoes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.requisicoes.domain.Transporte;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransporteCatalogoResponse {
    private Long id;
    private String codigo;
    private String tipo;
    private String categoria;
    private String matricula;
    private String marca;
    private String modelo;
    private Integer lotacao;
    private String dataMatricula;

    public static TransporteCatalogoResponse from(Transporte transporte) {
        return new TransporteCatalogoResponse(transporte.getId(), transporte.getCodigo(), transporte.getTipo(), transporte.getCategoria() != null ? transporte.getCategoria().name() : null, transporte.getMatricula(), transporte.getMarca(), transporte.getModelo(), transporte.getLotacao(), transporte.getDataMatricula() != null ? transporte.getDataMatricula().toString() : null);
    }
}
