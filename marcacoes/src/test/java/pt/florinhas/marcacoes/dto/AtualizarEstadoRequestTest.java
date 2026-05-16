package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.domain.EventoEstado;

class AtualizarEstadoRequestTest {

    @Test
    void deveCriarRequest() {

        AtualizarEstadoRequest request =
                new AtualizarEstadoRequest(
                        "CONCLUIDO",
                        1L,
                        2L,
                        "Motivo"
                );

        assertEquals("CONCLUIDO", request.getNovoEstado());
        assertEquals(1L, request.getFuncionarioId());
        assertEquals(2L, request.getVersion());
        assertEquals("Motivo", request.getMotivoCancelamento());
    }

    @Test
    void getNovoEstadoEnum_DeveConverterEnum() {

        AtualizarEstadoRequest request =
                new AtualizarEstadoRequest();

        request.setNovoEstado("CONCLUIDO");

        assertEquals(
                EventoEstado.CONCLUIDO,
                request.getNovoEstadoEnum()
        );
    }
}