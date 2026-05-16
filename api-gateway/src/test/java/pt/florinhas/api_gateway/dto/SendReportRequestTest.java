package pt.florinhas.api_gateway.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SendReportRequestTest {

    @Test
    void gettersAndSetters_DeveFuncionar() {

        SendReportRequest request =
                new SendReportRequest();

        request.setTo("teste@teste.com");
        request.setSubject("Assunto");
        request.setBody("Body");
        request.setPdfBase64("base64");
        request.setFileName("ficheiro.pdf");

        assertEquals(
                "teste@teste.com",
                request.getTo());

        assertEquals(
                "Assunto",
                request.getSubject());

        assertEquals(
                "Body",
                request.getBody());

        assertEquals(
                "base64",
                request.getPdfBase64());

        assertEquals(
                "ficheiro.pdf",
                request.getFileName());
    }
}