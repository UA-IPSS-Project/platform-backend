package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.domain.EventoEstado;

class AtualizarEstadoRequestTest {

    @Test
    void atualizarEstadoRequest_DeveConverterEnum() {

        AtualizarEstadoRequest request = new AtualizarEstadoRequest();

        request.setNovoEstado("AGENDADO");

        assertEquals(EventoEstado.AGENDADO, request.getNovoEstadoEnum());
    }

    @Test
    void atualizarEstadoRequest_DeveGuardarValores() {

        AtualizarEstadoRequest request =
                new AtualizarEstadoRequest();

        request.setNovoEstado("CONCLUIDO");
        request.setFuncionarioId(1L);
        request.setVersion(2L);
        request.setMotivoCancelamento("Teste");

        assertEquals("CONCLUIDO", request.getNovoEstado());
        assertEquals(1L, request.getFuncionarioId());
        assertEquals(2L, request.getVersion());
        assertEquals("Teste", request.getMotivoCancelamento());
    }
}