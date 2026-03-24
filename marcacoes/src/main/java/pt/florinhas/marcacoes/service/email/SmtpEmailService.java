package pt.florinhas.marcacoes.service.email;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'as' HH:mm");

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPassword(String to, String password) {
        sendEmail(
                to,
                "Acesso à Plataforma Florinhas",
                "Foi criada uma conta para si.\n\n" +
                "A sua password inicial é: " + password + "\n\n" +
                        "Por favor, altere a sua password após o primeiro login.");
    }

    @Override
    public void sendAppointmentCreated(String to, LocalDateTime appointmentDateTime) {
        String dateText = appointmentDateTime.format(DATE_TIME_FORMATTER);
        sendEmail(
                to,
                "Marcacao Criada",
                "A sua marcacao foi criada para " + dateText + ".");
    }

    @Override
    public void sendAppointmentCancelled(String to, String motivo) {
        sendEmail(
                to,
                "Marcacao Cancelada",
                "A sua marcacao foi cancelada por: " + (motivo == null || motivo.isBlank() ? "sem motivo especificado" : motivo)
                        + ".");
    }

    @Override
    public void sendAppointmentReminderOneDay(String to, LocalDateTime appointmentDateTime) {
        String dateText = appointmentDateTime.format(DATE_TIME_FORMATTER);
        sendEmail(
                to,
                "Lembrete de Marcacao (1 dia)",
                "Lembrete: tem uma marcacao em 1 dia, no dia " + dateText + ".");
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        log.info("Email enviado para {} com assunto '{}'", to, subject);
    }
}
