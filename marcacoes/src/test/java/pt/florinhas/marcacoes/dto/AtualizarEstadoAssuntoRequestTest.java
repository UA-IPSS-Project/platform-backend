package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AtualizarEstadoAssuntoRequestTest {

    @Test
    void deveCriarRequest() {

        AtualizarEstadoAssuntoRequest request =
                new AtualizarEstadoAssuntoRequest(true);

        assertTrue(request.ativo());
    }
}