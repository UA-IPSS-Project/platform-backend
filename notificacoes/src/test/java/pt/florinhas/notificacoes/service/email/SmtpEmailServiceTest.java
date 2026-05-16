package pt.florinhas.notificacoes.service.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

class SmtpEmailServiceTest {

    private JavaMailSender mailSender;

    private SmtpEmailService service;

    @BeforeEach
    void setUp() {

        mailSender =
                mock(JavaMailSender.class);

        service =
                new SmtpEmailService(mailSender);

        ReflectionTestUtils.setField(
                service,
                "fromEmail",
                "noreply@florinhas.pt"
        );
    }

    @Test
    void sendPassword_DeveEnviarEmail() {

        service.sendPassword(
                "teste@teste.com",
                "123456"
        );

        verify(mailSender)
                .send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAppointmentCancelled_DeveEnviarEmail() {

        service.sendAppointmentCancelled(
                "teste@teste.com",
                "Motivo"
        );

        verify(mailSender)
                .send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAppointmentReminderOneDay_DeveEnviarEmail() {

        service.sendAppointmentReminderOneDay(
                "teste@teste.com",
                LocalDateTime.now()
        );

        verify(mailSender)
                .send(any(SimpleMailMessage.class));
    }

    @Test
    void sendGenericEmail_DeveEnviarEmail() {

        service.sendGenericEmail(
                "teste@teste.com",
                "Assunto",
                "Mensagem"
        );

        verify(mailSender)
                .send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAppointmentCreated_DeveEnviarComAnexo() {

        MimeMessage mimeMessage =
                new MimeMessage(
                        Session.getDefaultInstance(
                                System.getProperties()
                        )
                );

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        service.sendAppointmentCreated(
                "teste@teste.com",
                LocalDateTime.now(),
                1L,
                "Consulta Psicologia",
                30
        );

        verify(mailSender)
                .send(any(MimeMessage.class));
    }

    @Test
    void sendEmailWithAttachment_DeveEnviar() {

        MimeMessage mimeMessage =
                new MimeMessage(
                        Session.getDefaultInstance(
                                System.getProperties()
                        )
                );

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        service.sendEmailWithAttachment(
                "teste@teste.com",
                "Assunto",
                "Mensagem",
                "conteudo".getBytes(),
                "teste.txt"
        );

        verify(mailSender)
                .send(any(MimeMessage.class));
    }

    @Test
    void sendEmailWithAttachment_DeveLancarErro() {

        when(mailSender.createMimeMessage())
                .thenThrow(
                        new RuntimeException("Erro")
                );

        assertThrows(
                RuntimeException.class,
                () -> service.sendEmailWithAttachment(
                        "teste@teste.com",
                        "Assunto",
                        "Mensagem",
                        "conteudo".getBytes(),
                        "teste.txt"
                )
        );
    }

    @Test
    void sendAppointmentCreated_DeveFazerFallback() {

        when(mailSender.createMimeMessage())
                .thenThrow(
                        new RuntimeException("Erro")
                );

        service.sendAppointmentCreated(
                "teste@teste.com",
                LocalDateTime.now(),
                1L,
                "Consulta",
                30
        );

        verify(mailSender)
                .send(any(SimpleMailMessage.class));
    }
}