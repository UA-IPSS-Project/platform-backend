package pt.florinhas.notificacoes.service.email;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;

class SmtpEmailServiceTest {

    private JavaMailSender mailSender;

    private SmtpEmailService service;

    @BeforeEach
    void setUp() throws Exception {

        mailSender =
                mock(JavaMailSender.class);

        service =
                new SmtpEmailService(
                        mailSender);

        setField(
                "fromEmail",
                "teste@test.com");
    }

    @Test
    void sendPassword_DeveEnviar() {

        service.sendPassword(
                "a@a.com",
                "123");

        verify(mailSender)
                .send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAppointmentCreated_DeveEnviar() {

        MimeMessage mimeMessage =
                mock(MimeMessage.class);

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        service.sendAppointmentCreated(
                "a@a.com",
                LocalDateTime.now(),
                1L,
                "Consulta",
                15);

        verify(mailSender)
                .send(any(MimeMessage.class));
    }

    @Test
    void sendAppointmentCancelled_DeveEnviar() {

        service.sendAppointmentCancelled(
                "a@a.com",
                "Nuno",
                LocalDateTime.now(),
                "Consulta",
                "Motivo");

        verify(mailSender)
                .send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAppointmentReminderOneDay_DeveEnviar() {

        service.sendAppointmentReminderOneDay(
                "a@a.com",
                LocalDateTime.now(),
                "Consulta");

        verify(mailSender)
                .send(any(SimpleMailMessage.class));
    }

    @Test
    void sendGenericEmail_DeveEnviar() {

        service.sendGenericEmail(
                "a@a.com",
                "Assunto",
                "Mensagem");

        verify(mailSender)
                .send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmailWithAttachment_DeveEnviar() {

        MimeMessage mimeMessage =
                mock(MimeMessage.class);

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        service.sendEmailWithAttachment(
                "a@a.com",
                "Assunto",
                "Mensagem",
                "abc".getBytes(),
                "teste.pdf");

        verify(mailSender)
                .send(any(MimeMessage.class));
    }

    @Test
    void sendAppointmentCreated_DeveFallbackParaEmailNormal() {

        doThrow(new RuntimeException())
                .when(mailSender)
                .send(any(MimeMessage.class));

        service.sendAppointmentCreated(
                "a@a.com",
                LocalDateTime.now(),
                1L,
                "Consulta",
                15);

        verify(mailSender)
                .send(any(SimpleMailMessage.class));
    }

    private void setField(
            String field,
            Object value)
            throws Exception {

        Field f =
                SmtpEmailService.class
                        .getDeclaredField(field);

        f.setAccessible(true);

        f.set(service, value);
    }
}