package pt.florinhas.marcacoes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class NotificacaoService {

    @Value("${notificacoes.url:http://notificacoes:8083}")
    private String notificacoesUrl;

    @Value("${gateway.shared-secret:}")
    private String gatewaySecret;

    private final RestTemplate restTemplate;

    public NotificacaoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Setters for tests
    public void setNotificacoesUrl(String url) { this.notificacoesUrl = url; }
    public void setGatewaySecret(String secret) { this.gatewaySecret = secret; }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final String METADATA_SUBTYPE_KEY = "notificationSubtype";
    private static final String MSG_PREFIX_UTENTE = "O utente ";
    private static final String METADATA_APPOINTMENT_ID = "appointmentId";
    private static final String TIPO_LEMBRETE = "LEMBRETE";

    public void criarNotificacao(Long utilizadorId, String titulo, String mensagem, String tipo) {
        enviarParaMicrosservico(utilizadorId, titulo, mensagem, tipo, null);
    }

    public void notificarNovaMarcacaoParaSecretaria(Long secretariaId, String nomeUtente, Long marcacaoId, LocalDateTime data, String assunto) {
        String dataFormatada = data.format(DATE_TIME_FORMATTER);
        String mensagem = MSG_PREFIX_UTENTE + nomeUtente + " criou uma marcação para " + dataFormatada + " — " + assunto;

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(METADATA_APPOINTMENT_ID, marcacaoId.toString());
        metadata.put("createdDate", data.format(DATE_FORMATTER));
        metadata.put("createdTime", data.format(TIME_FORMATTER));
        metadata.put(METADATA_SUBTYPE_KEY, "CREATED_BY_UTENTE");

        enviarParaMicrosservico(secretariaId, "Nova Marcação", mensagem, "SISTEMA", metadata);
    }

    public void notificarNovaMarcacao(Long utilizadorId, Long marcacaoId, LocalDateTime data) {
        String dataFormatada = data.format(DATE_TIME_FORMATTER);
        String mensagem = "Marcacao criada para " + dataFormatada + ".";
        String assunto = "Marcacao Criada";

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(METADATA_APPOINTMENT_ID, marcacaoId.toString());
        metadata.put("createdDate", data.format(DATE_FORMATTER));
        metadata.put("createdTime", data.format(TIME_FORMATTER));
        metadata.put(METADATA_SUBTYPE_KEY, "CREATED");

        enviarParaMicrosservico(utilizadorId, assunto, mensagem, TIPO_LEMBRETE, metadata);
    }

    public void notificarLembreteUmDia(Long utilizadorId, Long marcacaoId, LocalDateTime data) {
        String dataFormatada = data.format(DATE_TIME_FORMATTER);
        String mensagem = "Relembramos que tem uma marcação amanhã, " + dataFormatada + ".";
        String assunto = "Lembrete de Marcação";

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(METADATA_APPOINTMENT_ID, marcacaoId.toString());
        metadata.put("appointmentDate", data.format(DATE_FORMATTER));
        metadata.put("appointmentTime", data.format(TIME_FORMATTER));
        metadata.put(METADATA_SUBTYPE_KEY, "REMINDER_1_DAY");

        enviarParaMicrosservico(utilizadorId, assunto, mensagem, TIPO_LEMBRETE, metadata);
    }

    public void notificarCancelamento(Long utilizadorId, LocalDateTime data, String motivo) {
        String assunto = "Marcacao Cancelada";
        String motivoTexto = (motivo == null || motivo.isBlank()) ? "sem motivo especificado" : motivo;
        String mensagem = "Marcacao cancelada por " + motivoTexto + ".";

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("cancelledDate", data.format(DATE_FORMATTER));
        metadata.put("cancelledTime", data.format(TIME_FORMATTER));
        metadata.put(METADATA_SUBTYPE_KEY, "CANCELLED");

        enviarParaMicrosservico(utilizadorId, assunto, mensagem, "CANCELAMENTO", metadata);
    }

    public void notificarCancelamentoPeloUtente(Long destinatarioId, String nomeUtente, LocalDateTime data) {
        String dataFormatada = data.format(DATE_TIME_FORMATTER);
        String mensagem = MSG_PREFIX_UTENTE + nomeUtente + " cancelou a marcação de " + dataFormatada;
        String assunto = "Marcação Cancelada pelo Utente";

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("cancelledDate", data.format(DATE_FORMATTER));
        metadata.put("cancelledTime", data.format(TIME_FORMATTER));

        enviarParaMicrosservico(destinatarioId, assunto, mensagem, "CANCELAMENTO", metadata);
    }

    public void notificarDocumentosInvalidos(Long utilizadorId, String observacoes) {
        String mensagem = "Os documentos apresentados são inválidos. Por favor, contacte a secretaria. Observações: " + observacoes;
        String assunto = "Documentos Inválidos";

        enviarParaMicrosservico(utilizadorId, assunto, mensagem, TIPO_LEMBRETE, null);
    }

    public void notificarReagendamentoPeloUtente(Long destinatarioId, String nomeUtente, LocalDateTime dataAntiga, LocalDateTime dataNova) {
        String dataAntigaFmt = dataAntiga.format(DATE_TIME_FORMATTER);
        String dataNovaFmt = dataNova.format(DATE_TIME_FORMATTER);
        String mensagem = MSG_PREFIX_UTENTE + nomeUtente + " reagendou a marcação de " + dataAntigaFmt + " para " + dataNovaFmt;
        String assunto = "Marcação Reagendada pelo Utente";

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("oldDate", dataAntiga.format(DATE_FORMATTER));
        metadata.put("newDate", dataNova.format(DATE_FORMATTER));
        metadata.put(METADATA_SUBTYPE_KEY, "RESCHEDULED");

        enviarParaMicrosservico(destinatarioId, assunto, mensagem, TIPO_LEMBRETE, metadata);
    }

    private void enviarParaMicrosservico(Long utilizadorId, String titulo, String mensagem, String tipo, Map<String, Object> metadata) {
        try {
            String url = notificacoesUrl + "/api/internal/notificacoes/criar";
            Map<String, Object> request = new HashMap<>();
            request.put("utilizadorId", utilizadorId);
            request.put("titulo", titulo);
            request.put("mensagem", mensagem);
            request.put("tipo", tipo);
            request.put("metadata", metadata);

            HttpHeaders headers = new HttpHeaders();
            if (StringUtils.hasText(gatewaySecret)) {
                headers.set("X-Gateway-Secret", gatewaySecret);
            }
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(request, headers);

            restTemplate.postForObject(url, requestEntity, Void.class);
            log.info("Notificação enviada para o microsserviço de notificações: {}", titulo);
        } catch (Exception e) {
            log.error("Erro ao enviar notificação para o microsserviço", e);
        }
    }
}