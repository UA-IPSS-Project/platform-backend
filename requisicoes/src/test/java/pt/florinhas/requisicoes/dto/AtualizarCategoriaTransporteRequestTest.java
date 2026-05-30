package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pt.florinhas.requisicoes.domain.TransporteCategoria;

class AtualizarCategoriaTransporteRequestTest {

    @Test
    void constructorAndAccessors_DeveFuncionar() {

        AtualizarCategoriaTransporteRequest request =
                new AtualizarCategoriaTransporteRequest(
                        TransporteCategoria.AMBULANCIA);

        assertEquals(
                TransporteCategoria.AMBULANCIA,
                request.categoria());
    }
}