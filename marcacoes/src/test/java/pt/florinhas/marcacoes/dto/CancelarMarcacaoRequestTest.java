package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CancelarMarcacaoRequestTest {

    @Test
    void deveCriarRequest() {

        CancelarMarcacaoRequest request =
                new CancelarMarcacaoRequest(
                        "Motivo",
                        1L
                );

        assertEquals("Motivo", request.getMotivo());
        assertEquals(1L, request.getFuncionarioId());
    }
}