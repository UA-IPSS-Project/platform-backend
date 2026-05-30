package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pt.florinhas.requisicoes.domain.TransporteCategoria;

class MoverCategoriaTransporteRequestTest {

    @Test
    void constructorAndAccessors_DeveFuncionar() {

        MoverCategoriaTransporteRequest request =
                new MoverCategoriaTransporteRequest(
                        TransporteCategoria.ESCOLAR,
                        TransporteCategoria.ABATIDO_VENDIDO_DESCONTINUADO);

        assertEquals(
                TransporteCategoria.ESCOLAR,
                request.origem());

        assertEquals(
                TransporteCategoria.ABATIDO_VENDIDO_DESCONTINUADO,
                request.destino());
    }
}