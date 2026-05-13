package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SendReportRequestTest {

    @Test
    void deveDefinirValores() {

        SendReportRequest request =
                new SendReportRequest();

        request.setTo("test@test.com");
        request.setSubject("Relatório");
        request.setBody("Conteúdo");
        request.setPdfBase64("base64");
        request.setFileName("relatorio.pdf");

        assertEquals(
                "test@test.com",
                request.getTo()
        );

        assertEquals(
                "Relatório",
                request.getSubject()
        );

        assertEquals(
                "Conteúdo",
                request.getBody()
        );

        assertEquals(
                "base64",
                request.getPdfBase64()
        );

        assertEquals(
                "relatorio.pdf",
                request.getFileName()
        );
    }
}