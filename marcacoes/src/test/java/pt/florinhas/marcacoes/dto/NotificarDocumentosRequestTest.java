package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NotificarDocumentosRequestTest {

    @Test
    void notificarDocumentosRequest_DeveGuardarValores() {

        NotificarDocumentosRequest request = new NotificarDocumentosRequest();

        request.setObservacoes("Teste");
        request.setFuncionarioId(1L);

        assertEquals("Teste", request.getObservacoes());
        assertEquals(1L, request.getFuncionarioId());
    }
}