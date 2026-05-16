package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import pt.florinhas.marcacoes.dto.SendReportRequest;
import pt.florinhas.marcacoes.service.email.EmailService;

class ReportControllerTest {

    @Mock
    private EmailService emailService;

    private ReportController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new ReportController(emailService);
    }

    @Test
    void sendReportByEmail_DeveEnviarEmailGenericoQuandoNaoHaPdf() {
        SendReportRequest request = mock(SendReportRequest.class);

        when(request.getPdfBase64()).thenReturn(null);
        when(request.getTo()).thenReturn("user@test.com");
        when(request.getSubject()).thenReturn("Assunto");
        when(request.getBody()).thenReturn("Corpo");

        ResponseEntity<Void> result = controller.sendReportByEmail(request);

        assertEquals(200, result.getStatusCode().value());
        verify(emailService).sendGenericEmail("user@test.com", "Assunto", "Corpo");
        verify(emailService, never()).sendEmailWithAttachment(anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void sendReportByEmail_DeveEnviarComAnexoQuandoHaPdfBase64() {
        SendReportRequest request = mock(SendReportRequest.class);
        byte[] pdf = "pdf-content".getBytes();
        String base64 = Base64.getEncoder().encodeToString(pdf);

        when(request.getPdfBase64()).thenReturn(base64);
        when(request.getTo()).thenReturn("user@test.com");
        when(request.getSubject()).thenReturn("Assunto");
        when(request.getBody()).thenReturn("Corpo");
        when(request.getFileName()).thenReturn("relatorio.pdf");

        ResponseEntity<Void> result = controller.sendReportByEmail(request);

        assertEquals(200, result.getStatusCode().value());
        verify(emailService).sendEmailWithAttachment(
                eq("user@test.com"),
                eq("Assunto"),
                eq("Corpo"),
                eq(pdf),
                eq("relatorio.pdf"));
    }

    @Test
    void sendReportByEmail_DeveRemoverPrefixoDataUri() {
        SendReportRequest request = mock(SendReportRequest.class);
        byte[] pdf = "pdf-content".getBytes();
        String base64 = "data:application/pdf;base64," + Base64.getEncoder().encodeToString(pdf);

        when(request.getPdfBase64()).thenReturn(base64);
        when(request.getTo()).thenReturn("user@test.com");
        when(request.getSubject()).thenReturn("Assunto");
        when(request.getBody()).thenReturn("Corpo");
        when(request.getFileName()).thenReturn("meu.pdf");

        controller.sendReportByEmail(request);

        verify(emailService).sendEmailWithAttachment(
                eq("user@test.com"),
                eq("Assunto"),
                eq("Corpo"),
                eq(pdf),
                eq("meu.pdf"));
    }

    @Test
    void sendReportByEmail_DeveUsarNomeDefaultQuandoFileNameNull() {
        SendReportRequest request = mock(SendReportRequest.class);
        byte[] pdf = "pdf-content".getBytes();
        String base64 = Base64.getEncoder().encodeToString(pdf);

        when(request.getPdfBase64()).thenReturn(base64);
        when(request.getTo()).thenReturn("user@test.com");
        when(request.getSubject()).thenReturn("Assunto");
        when(request.getBody()).thenReturn("Corpo");
        when(request.getFileName()).thenReturn(null);

        controller.sendReportByEmail(request);

        verify(emailService).sendEmailWithAttachment(
                eq("user@test.com"),
                eq("Assunto"),
                eq("Corpo"),
                eq(pdf),
                eq("relatorio.pdf"));
    }
}