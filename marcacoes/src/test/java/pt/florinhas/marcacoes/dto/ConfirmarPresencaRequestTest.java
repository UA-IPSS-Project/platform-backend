package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConfirmarPresencaRequestTest {

    @Test
    void deveDefinirValores() {

        ConfirmarPresencaRequest request =
                new ConfirmarPresencaRequest();

        request.setPresencaConfirmada(true);
        request.setFuncionarioId(1L);

        assertTrue(request.getPresencaConfirmada());
        assertEquals(1L, request.getFuncionarioId());
    }
}