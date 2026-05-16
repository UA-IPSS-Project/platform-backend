package pt.florinhas.notificacoes.service.email;

import java.time.LocalDateTime;

public interface EmailService {
    void sendPassword(String to, String password);

    void sendAppointmentCreated(String to, LocalDateTime appointmentDateTime, Long appointmentId, String summary, int durationMinutes);

    void sendAppointmentCancelled(String to, String motivo);

    void sendAppointmentReminderOneDay(String to, LocalDateTime appointmentDateTime);
    void sendGenericEmail(String to, String subject, String body);
    void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String fileName);
}
