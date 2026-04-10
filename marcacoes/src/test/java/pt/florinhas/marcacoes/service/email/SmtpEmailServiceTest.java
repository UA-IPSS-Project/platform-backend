package pt.florinhas.marcacoes.service.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private SmtpEmailService smtpEmailService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(smtpEmailService, "fromEmail", "noreply@test.com");
    }

    @Test
    void sendPassword_DeveEnviarEmailSimples() {
        smtpEmailService.sendPassword("user@test.com", "abc123");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertEquals("noreply@test.com", sent.getFrom());
        assertArrayEquals(new String[]{"user@test.com"}, sent.getTo());
        assertEquals("Acesso à Plataforma Florinhas", sent.getSubject());
        assertTrue(sent.getText().contains("abc123"));
    }

    @Test
    void sendAppointmentCancelled_DeveEnviarEmailSimplesComMotivo() {
        smtpEmailService.sendAppointmentCancelled("user@test.com", "motivo teste");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertEquals("Marcacao Cancelada", sent.getSubject());
        assertTrue(sent.getText().contains("motivo teste"));
    }

    @Test
    void sendAppointmentCancelled_DeveUsarMotivoDefaultQuandoVazio() {
        smtpEmailService.sendAppointmentCancelled("user@test.com", " ");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertTrue(sent.getText().contains("sem motivo especificado"));
    }

    @Test
    void sendAppointmentReminderOneDay_DeveEnviarEmailSimples() {
        LocalDateTime date = LocalDateTime.of(2026, 4, 15, 10, 30);

        smtpEmailService.sendAppointmentReminderOneDay("user@test.com", date);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertEquals("Lembrete de Marcacao (1 dia)", sent.getSubject());
        assertTrue(sent.getText().contains("15/04/2026"));
    }

    @Test
    void sendGenericEmail_DeveEnviarEmailSimples() {
        smtpEmailService.sendGenericEmail("user@test.com", "Assunto", "Corpo");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertEquals("Assunto", sent.getSubject());
        assertEquals("Corpo", sent.getText());
    }

    @Test
    void sendEmailWithAttachment_DeveEnviarMimeMessage() throws Exception {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        byte[] attachment = "conteudo".getBytes();

        smtpEmailService.sendEmailWithAttachment(
                "user@test.com",
                "Assunto",
                "Corpo",
                attachment,
                "teste.ics"
        );

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmailWithAttachment_DeveLancarRuntimeExceptionQuandoFalha() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("smtp down"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> smtpEmailService.sendEmailWithAttachment(
                        "user@test.com",
                        "Assunto",
                        "Corpo",
                        "conteudo".getBytes(),
                        "teste.ics"
                ));

        assertEquals("Erro ao enviar email com anexo", ex.getMessage());
    }

    @Test
    void sendAppointmentCreated_DeveEnviarMimeMessageComAnexo() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        LocalDateTime date = LocalDateTime.of(2026, 4, 20, 14, 0);

        smtpEmailService.sendAppointmentCreated(
                "user@test.com",
                date,
                10L,
                "Consulta Geral",
                15
        );

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendAppointmentCreated_DeveFazerFallbackParaEmailSimplesQuandoAnexoFalha() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("mime fail"));

        LocalDateTime date = LocalDateTime.of(2026, 4, 20, 14, 0);

        smtpEmailService.sendAppointmentCreated(
                "user@test.com",
                date,
                10L,
                "Consulta Geral",
                15
        );

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertEquals("Marcação Criada", sent.getSubject());
        assertTrue(sent.getText().contains("20/04/2026"));
    }
}