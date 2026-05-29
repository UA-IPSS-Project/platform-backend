package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConfirmarPresencaRequestTest {

    @Test
    void confirmarPresencaRequest_DeveGuardarValores() {

        ConfirmarPresencaRequest request = new ConfirmarPresencaRequest();

        request.setPresencaConfirmada(true);
        request.setFuncionarioId(1L);

        assertEquals(true, request.getPresencaConfirmada());
        assertEquals(1L, request.getFuncionarioId());
    }
}