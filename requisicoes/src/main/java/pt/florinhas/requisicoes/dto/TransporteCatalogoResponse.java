package pt.florinhas.requisicoes.dto;

import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.domain.TransporteCategoria;

public record TransporteCatalogoResponse(
        Long id,
        String codigo,
        String tipo,
        TransporteCategoria categoria,
        String matricula,
        String marca,
        String modelo,
        Integer lotacao,
        String dataMatricula) {

    public static TransporteCatalogoResponse from(Transporte transporte) {
        return new TransporteCatalogoResponse(
                transporte.getId(),
                transporte.getCodigo(),
                transporte.getTipo(),
                transporte.getCategoria(),
                transporte.getMatricula(),
                transporte.getMarca(),
                transporte.getModelo(),
                transporte.getLotacao(),
                transporte.getDataMatricula() != null ? transporte.getDataMatricula().toString() : null);
    }
}