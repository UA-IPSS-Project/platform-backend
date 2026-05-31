package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class SendReportRequestTest {

    @Test
    void sendReportRequest_DeveGuardarValores() {

        SendReportRequest request = new SendReportRequest();

        request.setTo("teste@teste.com");
        request.setPdfBase64("abc");
        request.setFileName("teste.pdf");
        request.setPeriodoInicio("2026-01-01");
        request.setPeriodoFim("2026-01-31");
        request.setSeccoes(List.of("SECRETARIA"));

        assertEquals("teste@teste.com", request.getTo());
        assertEquals("abc", request.getPdfBase64());
        assertEquals("teste.pdf", request.getFileName());
        assertEquals("2026-01-01", request.getPeriodoInicio());
        assertEquals("2026-01-31", request.getPeriodoFim());
        assertEquals(1, request.getSeccoes().size());
    }
}