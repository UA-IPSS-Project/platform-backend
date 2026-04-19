package pt.florinhas.notificacoes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import pt.florinhas.common_data.domain.NotificacaoTipo;
import pt.florinhas.notificacoes.service.NotificacaoService;
import pt.florinhas.notificacoes.service.email.EmailService;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/notificacoes")
@RequiredArgsConstructor
public class InternalCommunicationController {

    private final NotificacaoService notificacaoService;
    private final EmailService emailService;

    @PostMapping("/criar")
    public ResponseEntity<Void> criarNotificacao(@RequestBody CriarNotificacaoRequest req) {
        notificacaoService.criarNotificacao(req.getUtilizadorId(), req.getTitulo(), req.getMensagem(), req.getTipo(), req.getMetadata());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/password")
    public ResponseEntity<Void> enviarPassword(@RequestBody EmailPasswordRequest req) {
        emailService.sendPassword(req.getTo(), req.getPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/marcacao-criada")
    public ResponseEntity<Void> enviarMarcacaoCriada(@RequestBody EmailMarcacaoCriadaRequest req) {
        emailService.sendAppointmentCreated(req.getTo(), req.getAppointmentDateTime(), req.getAppointmentId(), req.getSummary(), req.getDurationMinutes());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/marcacao-cancelada")
    public ResponseEntity<Void> enviarMarcacaoCancelada(@RequestBody EmailMarcacaoCanceladaRequest req) {
        emailService.sendAppointmentCancelled(req.getTo(), req.getMotivo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/marcacao-lembrete")
    public ResponseEntity<Void> enviarMarcacaoLembrete(@RequestBody EmailMarcacaoLembreteRequest req) {
        emailService.sendAppointmentReminderOneDay(req.getTo(), req.getAppointmentDateTime());
        return ResponseEntity.ok().build();
    }

    @Data
    public static class CriarNotificacaoRequest {
        private Long utilizadorId;
        private String titulo;
        private String mensagem;
        private NotificacaoTipo tipo;
        private Map<String, Object> metadata;
    }

    @Data
    public static class EmailPasswordRequest {
        private String to;
        private String password;
    }

    @Data
    public static class EmailMarcacaoCriadaRequest {
        private String to;
        private LocalDateTime appointmentDateTime;
        private Long appointmentId;
        private String summary;
        private int durationMinutes;
    }

    @Data
    public static class EmailMarcacaoCanceladaRequest {
        private String to;
        private String motivo;
    }

    @Data
    public static class EmailMarcacaoLembreteRequest {
        private String to;
        private LocalDateTime appointmentDateTime;
    }
}
