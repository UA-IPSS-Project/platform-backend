package pt.florinhas.marcacoes.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EmailServiceClientImpl implements EmailService {

    @Value("${notificacoes.url:http://notificacoes:8080}")
    private String notificacoesUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendPassword(String to, String password) {
        try {
            String url = notificacoesUrl + "/api/internal/notificacoes/email/password";
            Map<String, Object> req = new HashMap<>();
            req.put("to", to);
            req.put("password", password);
            restTemplate.postForObject(url, req, Void.class);
        } catch (Exception e) {
            log.error("Erro ao solicitar envio de password via notificacoes para {}", to, e);
        }
    }

    @Override
    public void sendAppointmentCreated(String to, LocalDateTime appointmentDateTime, Long appointmentId, String summary, int durationMinutes) {
        try {
            String url = notificacoesUrl + "/api/internal/notificacoes/email/marcacao-criada";
            Map<String, Object> req = new HashMap<>();
            req.put("to", to);
            req.put("appointmentDateTime", appointmentDateTime);
            req.put("appointmentId", appointmentId);
            req.put("summary", summary);
            req.put("durationMinutes", durationMinutes);
            restTemplate.postForObject(url, req, Void.class);
        } catch (Exception e) {
            log.error("Erro ao solicitar envio de marcacao criada via notificacoes para {}", to, e);
        }
    }

    @Override
    public void sendAppointmentCancelled(String to, String motivo) {
        try {
            String url = notificacoesUrl + "/api/internal/notificacoes/email/marcacao-cancelada";
            Map<String, Object> req = new HashMap<>();
            req.put("to", to);
            req.put("motivo", motivo);
            restTemplate.postForObject(url, req, Void.class);
        } catch (Exception e) {
            log.error("Erro ao solicitar envio de marcacao cancelada via notificacoes para {}", to, e);
        }
    }

    @Override
    public void sendAppointmentReminderOneDay(String to, LocalDateTime appointmentDateTime) {
        try {
            String url = notificacoesUrl + "/api/internal/notificacoes/email/marcacao-lembrete";
            Map<String, Object> req = new HashMap<>();
            req.put("to", to);
            req.put("appointmentDateTime", appointmentDateTime);
            restTemplate.postForObject(url, req, Void.class);
        } catch (Exception e) {
            log.error("Erro ao solicitar envio de lembrete via notificacoes para {}", to, e);
        }
    }

    @Override
    public void sendGenericEmail(String to, String subject, String body) {
        log.warn("sendGenericEmail via HTTP client not fully implemented yet");
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String fileName) {
        log.warn("sendEmailWithAttachment via HTTP client not fully implemented yet");
    }
}
