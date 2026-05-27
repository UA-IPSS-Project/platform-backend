package pt.florinhas.marcacoes.service.email;

import java.time.LocalDateTime;

public interface EmailService {
    void sendPassword(String to, String password);

    void sendAppointmentCreated(String to, LocalDateTime appointmentDateTime, Long appointmentId, String summary, int durationMinutes);

    void sendAppointmentCancelled(String to, String cancelledBy, LocalDateTime appointmentDateTime, String summary, String motivo);

    void sendAppointmentReminderOneDay(String to, LocalDateTime appointmentDateTime, String summary);

    void sendGenericEmail(String to, String subject, String body);

    void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String fileName);
}
