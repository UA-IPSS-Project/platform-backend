package pt.florinhas.notificacoes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

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
                        emailService
                );
    }

    @Test
    void criarNotificacao_DeveRetornar200() {

        var req =
                new InternalCommunicationController
                        .CriarNotificacaoRequest();

        req.setUtilizadorId(1L);
        req.setTitulo("Titulo");
        req.setMensagem("Mensagem");
        req.setTipo("INFO");
        req.setMetadata(Map.of());

        ResponseEntity<Void> response =
                controller.criarNotificacao(req);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        verify(notificacaoService)
                .criarNotificacao(
                        eq(1L),
                        eq("Titulo"),
                        eq("Mensagem"),
                        eq("INFO"),
                        anyMap()
                );
    }

    @Test
    void enviarPassword_DeveEnviar() {

        var req =
                new InternalCommunicationController
                        .EmailPasswordRequest();

        req.setTo("teste@teste.com");
        req.setPassword("123");

        ResponseEntity<Void> response =
                controller.enviarPassword(req);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        verify(emailService)
                .sendPassword(
                        "teste@teste.com",
                        "123"
                );
    }

    @Test
    void enviarMarcacaoCriada_DeveEnviar() {

        LocalDateTime data =
                LocalDateTime.now();

        var req =
                new InternalCommunicationController
                        .EmailMarcacaoCriadaRequest();

        req.setTo("teste@teste.com");
        req.setAppointmentDateTime(data);
        req.setAppointmentId(5L);
        req.setSummary("Consulta");
        req.setDurationMinutes(30);

        ResponseEntity<Void> response =
                controller.enviarMarcacaoCriada(req);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        verify(emailService)
                .sendAppointmentCreated(
                        eq("teste@teste.com"),
                        eq(data),
                        eq(5L),
                        eq("Consulta"),
                        eq(30)
                );
    }

    @Test
    void enviarMarcacaoCancelada_DeveEnviar() {

        var req =
                new InternalCommunicationController
                        .EmailMarcacaoCanceladaRequest();

        req.setTo("teste@teste.com");
        req.setMotivo("Motivo");

        ResponseEntity<Void> response =
                controller.enviarMarcacaoCancelada(req);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        verify(emailService)
                .sendAppointmentCancelled(
                        "teste@teste.com",
                        "Motivo"
                );
    }

    @Test
    void enviarMarcacaoLembrete_DeveEnviar() {

        LocalDateTime data =
                LocalDateTime.now();

        var req =
                new InternalCommunicationController
                        .EmailMarcacaoLembreteRequest();

        req.setTo("teste@teste.com");
        req.setAppointmentDateTime(data);

        ResponseEntity<Void> response =
                controller.enviarMarcacaoLembrete(req);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        verify(emailService)
                .sendAppointmentReminderOneDay(
                        "teste@teste.com",
                        data
                );
    }

    @Test
    void enviarEmailGenerico_DeveEnviar() {

        var req =
                new InternalCommunicationController
                        .EmailGenericoRequest();

        req.setTo("teste@teste.com");
        req.setSubject("Assunto");
        req.setBody("Body");

        ResponseEntity<Void> response =
                controller.enviarEmailGenerico(req);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        verify(emailService)
                .sendGenericEmail(
                        "teste@teste.com",
                        "Assunto",
                        "Body"
                );
    }
}