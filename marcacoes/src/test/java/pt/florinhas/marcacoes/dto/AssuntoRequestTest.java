package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AssuntoRequestTest {

    @Test
    void assuntoRequest_DeveGuardarNome() {

        AssuntoRequest request = new AssuntoRequest("Teste");

        assertEquals("Teste", request.nome());
    }
}