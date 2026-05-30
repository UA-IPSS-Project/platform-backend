package pt.florinhas.notificacoes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import pt.florinhas.notificacoes.controller.InternalCommunicationController.CriarNotificacaoRequest;
import pt.florinhas.notificacoes.controller.InternalCommunicationController.EmailAttachmentRequest;
import pt.florinhas.notificacoes.controller.InternalCommunicationController.EmailGenericoRequest;
import pt.florinhas.notificacoes.controller.InternalCommunicationController.EmailMarcacaoCanceladaRequest;
import pt.florinhas.notificacoes.controller.InternalCommunicationController.EmailMarcacaoCriadaRequest;
import pt.florinhas.notificacoes.controller.InternalCommunicationController.EmailMarcacaoLembreteRequest;
import pt.florinhas.notificacoes.controller.InternalCommunicationController.EmailPasswordRequest;
import pt.florinhas.notificacoes.service.NotificacaoService;
import pt.florinhas.notificacoes.service.email.EmailService;

class InternalCommunicationControllerTest {

    private NotificacaoService notificacaoService;
    private EmailService emailService;

    private InternalCommunicationController controller;

    @BeforeEach
    void setUp() {

        notificacaoService =
                mock(NotificacaoService.class);

        emailService =
                mock(EmailService.class);

        controller =
                new InternalCommunicationController(
                        notificacaoService,
                        emailService);
    }

    @Test
    void criarNotificacao_DeveExecutarService() {

        CriarNotificacaoRequest request =
                new CriarNotificacaoRequest();

        request.setUtilizadorId(1L);
        request.setTitulo("Titulo");
        request.setMensagem("Mensagem");
        request.setTipo("INFO");
        request.setMetadata(
                Map.of("a", "b"));

        ResponseEntity<Void> result =
                controller.criarNotificacao(request);

        assertEquals(
                200,
                result.getStatusCode().value());

        verify(notificacaoService)
                .criarNotificacao(
                        1L,
                        "Titulo",
                        "Mensagem",
                        "INFO",
                        Map.of("a", "b"));
    }

    @Test
    void enviarPassword_DeveExecutarService() {

        EmailPasswordRequest request =
                new EmailPasswordRequest();

        request.setTo("teste@test.com");
        request.setPassword("123");

        ResponseEntity<Void> result =
                controller.enviarPassword(request);

        assertEquals(
                200,
                result.getStatusCode().value());

        verify(emailService)
                .sendPassword(
                        "teste@test.com",
                        "123");
    }

    @Test
    void enviarMarcacaoCriada_DeveExecutarService() {

        EmailMarcacaoCriadaRequest request =
                new EmailMarcacaoCriadaRequest();

        request.setTo("teste@test.com");
        request.setAppointmentDateTime(
                LocalDateTime.now());
        request.setAppointmentId(1L);
        request.setSummary("Consulta");
        request.setDurationMinutes(15);

        ResponseEntity<Void> result =
                controller.enviarMarcacaoCriada(request);

        assertEquals(
                200,
                result.getStatusCode().value());

        verify(emailService)
                .sendAppointmentCreated(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(Integer.class));
    }

    @Test
    void enviarMarcacaoCancelada_DeveExecutarService() {

        EmailMarcacaoCanceladaRequest request =
                new EmailMarcacaoCanceladaRequest();

        request.setTo("teste@test.com");
        request.setCancelledBy("Nuno");
        request.setAppointmentDateTime(
                LocalDateTime.now());
        request.setSummary("Consulta");
        request.setMotivo("Motivo");

        ResponseEntity<Void> result =
                controller.enviarMarcacaoCancelada(
                        request);

        assertEquals(
                200,
                result.getStatusCode().value());

        verify(emailService)
                .sendAppointmentCancelled(
                        any(),
                        any(),
                        any(),
                        any(),
                        any());
    }

    @Test
    void enviarMarcacaoLembrete_DeveExecutarService() {

        EmailMarcacaoLembreteRequest request =
                new EmailMarcacaoLembreteRequest();

        request.setTo("teste@test.com");
        request.setAppointmentDateTime(
                LocalDateTime.now());
        request.setSummary("Consulta");

        ResponseEntity<Void> result =
                controller.enviarMarcacaoLembrete(
                        request);

        assertEquals(
                200,
                result.getStatusCode().value());

        verify(emailService)
                .sendAppointmentReminderOneDay(
                        any(),
                        any(),
                        any());
    }

    @Test
    void enviarEmailGenerico_DeveExecutarService() {

        EmailGenericoRequest request =
                new EmailGenericoRequest();

        request.setTo("teste@test.com");
        request.setSubject("Assunto");
        request.setBody("Mensagem");

        ResponseEntity<Void> result =
                controller.enviarEmailGenerico(
                        request);

        assertEquals(
                200,
                result.getStatusCode().value());

        verify(emailService)
                .sendGenericEmail(
                        "teste@test.com",
                        "Assunto",
                        "Mensagem");
    }

    @Test
    void enviarEmailComAnexo_DeveExecutarService() {

        EmailAttachmentRequest request =
                new EmailAttachmentRequest();

        request.setTo("teste@test.com");
        request.setSubject("Assunto");
        request.setBody("Mensagem");
        request.setFileName("teste.pdf");

        request.setAttachmentBase64(
                Base64.getEncoder()
                        .encodeToString(
                                "abc".getBytes()));

        ResponseEntity<Void> result =
                controller.enviarEmailComAnexo(
                        request);

        assertEquals(
                200,
                result.getStatusCode().value());

        verify(emailService)
                .sendEmailWithAttachment(
                        any(),
                        any(),
                        any(),
                        any(),
                        any());
    }
}