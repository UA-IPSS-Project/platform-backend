package pt.florinhas.api_gateway.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SendReportRequestTest {

    @Test
    void sendReportRequest_DeveGuardarValores() {

        SendReportRequest request =
                new SendReportRequest();

        request.setTo("teste@teste.com");
        request.setSubject("Relatório");
        request.setBody("Body");
        request.setPdfBase64("pdf");
        request.setFileName("relatorio.pdf");

        assertEquals(
                "teste@teste.com",
                request.getTo());

        assertEquals(
                "Relatório",
                request.getSubject());

        assertEquals(
                "Body",
                request.getBody());

        assertEquals(
                "pdf",
                request.getPdfBase64());

        assertEquals(
                "relatorio.pdf",
                request.getFileName());
    }
}