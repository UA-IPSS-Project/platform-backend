package pt.florinhas.notificacoes.service.email;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
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
                "Acesso à Plataforma Florinhas do Vouga",
                "Bem-vindo à plataforma Florinhas do Vouga!\n\n"
                + "Foi criada uma conta para si. Seguem os seus dados de acesso:\n\n"
                + "Password: " + password + "\n\n"
                + "Por favor, altere a sua password após o primeiro login por motivos de segurança.\n\n"
                + "Com os melhores cumprimentos,\n"
                + "Florinhas do Vouga");
    }

    @Override
    public void sendAppointmentCreated(String to, LocalDateTime appointmentDateTime, Long appointmentId, String summary, int durationMinutes) {
        String dateText = appointmentDateTime.format(DATE_TIME_FORMATTER);
        String assunto = summary != null ? summary : "Geral";
        String subject = "Marcação Criada — " + assunto;
        String body = "A sua marcação foi criada com sucesso.\n\n"
                + "Assunto: " + assunto + "\n"
                + "Data: " + dateText + "\n"
                + "Duração estimada: " + durationMinutes + " minutos\n\n"
                + "Para mais detalhes, pode consultar a sua marcação na plataforma Florinhas do Vouga acedendo à sua conta.\n\n"
                + "Com os melhores cumprimentos,\n"
                + "Florinhas do Vouga";
        
        // Dynamic filename: marcacao-assunto.ics (lowercase, spaces replaced by hyphens)
        String safeSummary = summary != null ? summary.toLowerCase().trim().replaceAll("\\s+", "-") : "geral";
        String fileName = "marcacao-" + safeSummary + ".ics";
        
        try {
            byte[] icsBytes = generateIcs(appointmentId, appointmentDateTime, summary, durationMinutes);
            sendEmailWithAttachment(to, subject, body, icsBytes, fileName);
        } catch (Exception e) {
            log.error("Erro ao gerar ou enviar convite ICS para {}", to, e);
            sendEmail(to, subject, body);
        }
    }

    private byte[] generateIcs(Long id, LocalDateTime start, String summary, int duration) {
        LocalDateTime end = start.plusMinutes(duration);
        DateTimeFormatter icsFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        
        StringBuilder b = new StringBuilder();
        b.append("BEGIN:VCALENDAR\r\n");
        b.append("VERSION:2.0\r\n");
        b.append("PRODID:-//Florinhas do Vouga//NONSGML v1.0//EN\r\n");
        b.append("METHOD:REQUEST\r\n");
        b.append("CALSCALE:GREGORIAN\r\n");
        b.append("BEGIN:VEVENT\r\n");
        b.append("UID:").append(id).append("@florinhas.pt\r\n");
        b.append("DTSTAMP:").append(LocalDateTime.now().format(icsFormatter)).append("\r\n");
        b.append("DTSTART:").append(start.format(icsFormatter)).append("\r\n");
        b.append("DTEND:").append(end.format(icsFormatter)).append("\r\n");
        b.append("SUMMARY:").append(summary != null ? summary : "Marcação - Florinhas do Vouga").append("\r\n");
        b.append("DESCRIPTION:Sua marcação na Florinhas do Vouga está agendada para ").append(start.format(DATE_TIME_FORMATTER)).append(".\r\n");
        b.append("END:VEVENT\r\n");
        b.append("END:VCALENDAR\r\n");
        
        return b.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public void sendAppointmentCancelled(String to, String cancelledBy, LocalDateTime appointmentDateTime, String summary, String motivo) {
        String dateText = appointmentDateTime != null ? appointmentDateTime.format(DATE_TIME_FORMATTER) : "não especificada";
        String assunto = summary != null ? summary : "Geral";
        String motivoTexto = (motivo == null || motivo.isBlank()) ? "sem motivo especificado" : motivo;
        String canceladoPor = (cancelledBy == null || cancelledBy.isBlank()) ? "a instituição" : cancelledBy;

        sendEmail(
                to,
                "Marcação Cancelada — " + assunto,
                "A sua marcação foi cancelada.\n\n"
                        + "Assunto: " + assunto + "\n"
                        + "Data: " + dateText + "\n"
                        + "Cancelada por: " + canceladoPor + "\n"
                        + "Motivo: " + motivoTexto + "\n\n"
                        + "Para mais informações, pode consultar a sua conta na plataforma Florinhas do Vouga ou contactar a secretaria.\n\n"
                        + "Com os melhores cumprimentos,\n"
                        + "Florinhas do Vouga");
    }

    @Override
    public void sendAppointmentReminderOneDay(String to, LocalDateTime appointmentDateTime, String summary) {
        String dateText = appointmentDateTime.format(DATE_TIME_FORMATTER);
        String assunto = summary != null ? summary : "Geral";
        sendEmail(
                to,
                "Lembrete de Marcação — " + assunto,
                "Relembramos que tem uma marcação agendada para amanhã.\n\n"
                        + "Assunto: " + assunto + "\n"
                        + "Data: " + dateText + "\n\n"
                        + "Para mais detalhes, pode consultar a sua conta na plataforma Florinhas do Vouga.\n\n"
                        + "Com os melhores cumprimentos,\n"
                        + "Florinhas do Vouga");
    }

    @Override
    public void sendGenericEmail(String to, String subject, String body) {
        sendEmail(to, subject, body);
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String fileName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // Use true to indicate we need a multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            // Add the attachment with explicit MIME type
            helper.addAttachment(fileName, new ByteArrayResource(attachment), "text/calendar; method=REQUEST; charset=UTF-8");

            mailSender.send(message);
            log.info("Email com anexo '{}' enviado para {}", fileName, to);
        } catch (Exception e) {
            log.error("Falha ao enviar email com anexo para {}", to, e);
            throw new RuntimeException("Erro ao enviar email com anexo", e);
        }
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
