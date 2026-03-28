package pt.florinhas.marcacoes.service.email;

import java.time.LocalDateTime;

public interface EmailService {
    void sendPassword(String to, String password);

    void sendAppointmentCreated(String to, LocalDateTime appointmentDateTime);

    void sendAppointmentCancelled(String to, String motivo);

    void sendAppointmentReminderOneDay(String to, LocalDateTime appointmentDateTime);
    void sendGenericEmail(String to, String subject, String body);
}
