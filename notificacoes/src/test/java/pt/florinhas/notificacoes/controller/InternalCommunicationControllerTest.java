package pt.florinhas.notificacoes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

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

                notificacaoService = org.mockito.Mockito.mock(
                                NotificacaoService.class);

                emailService = org.mockito.Mockito.mock(
                                EmailService.class);

                controller = new InternalCommunicationController(
                                notificacaoService,
                                emailService);
        }

        @Test
        void criarNotificacao_DeveRetornarOk() {

                var request = new InternalCommunicationController.CriarNotificacaoRequest();

                request.setUtilizadorId(1L);

                request.setTitulo("Titulo");

                request.setMensagem("Mensagem");

                request.setTipo("INFO");

                ResponseEntity<Void> response = controller.criarNotificacao(
                                request);

                assertEquals(
                                200,
                                response.getStatusCode().value());

                verify(notificacaoService)
                                .criarNotificacao(
                                                1L,
                                                "Titulo",
                                                "Mensagem",
                                                "INFO",
                                                null);
        }

        @Test
        void enviarPassword_DeveExecutar() {

                var request = new InternalCommunicationController.EmailPasswordRequest();

                request.setTo("teste@test.com");

                request.setPassword("123");

                controller.enviarPassword(
                                request);

                verify(emailService)
                                .sendPassword(
                                                "teste@test.com",
                                                "123");
        }

        @Test
        void enviarMarcacaoCriada_DeveExecutar() {

                var request = new InternalCommunicationController.EmailMarcacaoCriadaRequest();

                request.setTo("teste@test.com");

                request.setAppointmentDateTime(
                                LocalDateTime.now());

                request.setAppointmentId(1L);

                request.setSummary("Teste");

                request.setDurationMinutes(30);

                controller.enviarMarcacaoCriada(
                                request);

                verify(emailService)
                                .sendAppointmentCreated(
                                                org.mockito.ArgumentMatchers.anyString(),
                                                org.mockito.ArgumentMatchers.any(),
                                                org.mockito.ArgumentMatchers.anyLong(),
                                                org.mockito.ArgumentMatchers.anyString(),
                                                org.mockito.ArgumentMatchers.anyInt());
        }
}