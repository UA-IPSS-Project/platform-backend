package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AssuntoRequestTest {

    @Test
    void deveCriarAssuntoRequest() {

        AssuntoRequest request =
                new AssuntoRequest("Consulta");

        assertEquals(
                "Consulta",
                request.nome()
        );
    }
}