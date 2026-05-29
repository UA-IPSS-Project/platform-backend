package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CancelarMarcacaoRequestTest {

    @Test
    void cancelarMarcacaoRequest_DeveGuardarValores() {

        CancelarMarcacaoRequest request = new CancelarMarcacaoRequest();

        request.setMotivo("Teste");
        request.setFuncionarioId(1L);

        assertEquals("Teste", request.getMotivo());
        assertEquals(1L, request.getFuncionarioId());
    }
}