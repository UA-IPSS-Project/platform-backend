package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AtualizarEstadoAssuntoRequestTest {

    @Test
    void atualizarEstadoAssuntoRequest_DeveGuardarValor() {

        AtualizarEstadoAssuntoRequest request = new AtualizarEstadoAssuntoRequest(true);

        assertEquals(true, request.ativo());
    }
}