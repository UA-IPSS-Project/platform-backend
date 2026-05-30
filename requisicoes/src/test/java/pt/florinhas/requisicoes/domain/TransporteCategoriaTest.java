package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TransporteCategoriaTest {

    @Test
    void values_DeveConterTodos() {

        assertEquals(
                true,
                TransporteCategoria.values().length > 10);

        assertEquals(
                TransporteCategoria.LIGEIRO_DE_PASSAGEIROS,
                TransporteCategoria.valueOf(
                        "LIGEIRO_DE_PASSAGEIROS"));

        assertEquals(
                TransporteCategoria.AMBULANCIA,
                TransporteCategoria.valueOf(
                        "AMBULANCIA"));

        assertEquals(
                TransporteCategoria.ABATIDO_VENDIDO_DESCONTINUADO,
                TransporteCategoria.valueOf(
                        "ABATIDO_VENDIDO_DESCONTINUADO"));
    }
}