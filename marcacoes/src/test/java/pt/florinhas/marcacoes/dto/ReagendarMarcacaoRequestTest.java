package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ReagendarMarcacaoRequestTest {

    @Test
    void reagendarMarcacaoRequest_DeveGuardarValores() {

        ReagendarMarcacaoRequest request = new ReagendarMarcacaoRequest();

        LocalDateTime data = LocalDateTime.now();

        request.setNovaDataHora(data);

        assertEquals(data, request.getNovaDataHora());
    }
}