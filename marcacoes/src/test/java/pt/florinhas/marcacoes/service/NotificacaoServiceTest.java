package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

class NotificacaoServiceTest {

    private RestTemplate restTemplate;
    private NotificacaoService service;

    @BeforeEach
    void setup() {
        restTemplate = mock(RestTemplate.class);
        service = new NotificacaoService(restTemplate);
        service.setNotificacoesUrl("http://localhost:8083");
        service.setGatewaySecret("secret-key-123");
    }

    @SuppressWarnings("unchecked")
    private <T> ArgumentCaptor<T> castCaptor(ArgumentCaptor<?> captor) {
        return (ArgumentCaptor<T>) captor;
    }

    @Test
    @DisplayName("criarNotificacao deve enviar requisição HTTP POST correta via RestTemplate")
    void criarNotificacao_DeveEnviarRequestCorreto() {
        service.criarNotificacao(1L, "Titulo", "Mensagem", "INFO");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = castCaptor(ArgumentCaptor.forClass(HttpEntity.class));

        verify(restTemplate).postForObject(
                urlCaptor.capture(),
                entityCaptor.capture(),
                eq(Void.class));

        assertEquals("http://localhost:8083/api/internal/notificacoes/criar", urlCaptor.getValue());

        HttpEntity<Map<String, Object>> entity = entityCaptor.getValue();
        assertNotNull(entity);
        assertEquals("secret-key-123", entity.getHeaders().getFirst("X-Gateway-Secret"));

        Map<String, Object> body = entity.getBody();
        assertNotNull(body);
        assertEquals(1L, body.get("utilizadorId"));
        assertEquals("Titulo", body.get("titulo"));
        assertEquals("Mensagem", body.get("mensagem"));
        assertEquals("INFO", body.get("tipo"));
        assertNull(body.get("metadata"));
    }

    @Test
    @DisplayName("notificarNovaMarcacaoParaSecretaria deve mapear metadados corretos para nova marcação")
    @SuppressWarnings("unchecked")
    void notificarNovaMarcacaoParaSecretaria_DeveEnviarRequestComMetadata() {
        LocalDateTime data = LocalDateTime.of(2026, 5, 20, 10, 30);
        service.notificarNovaMarcacaoParaSecretaria(2L, "Manuel Silva", 100L, data, "Consulta");

        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = castCaptor(ArgumentCaptor.forClass(HttpEntity.class));
        verify(restTemplate).postForObject(anyString(), entityCaptor.capture(), eq(Void.class));

        HttpEntity<Map<String, Object>> entity = entityCaptor.getValue();
        Map<String, Object> body = entity.getBody();
        assertNotNull(body);
        assertEquals(2L, body.get("utilizadorId"));
        assertEquals("Nova Marcação", body.get("titulo"));
        assertTrue(body.get("mensagem").toString().contains("Manuel Silva"));
        assertEquals("SISTEMA", body.get("tipo"));

        Map<String, Object> metadata = (Map<String, Object>) body.get("metadata");
        assertNotNull(metadata);
        assertEquals("100", metadata.get("appointmentId"));
        assertEquals("2026-05-20", metadata.get("createdDate"));
        assertEquals("10:30", metadata.get("createdTime"));
        assertEquals("CREATED_BY_UTENTE", metadata.get("notificationSubtype"));
    }

    @Test
    @DisplayName("notificarNovaMarcacao deve mapear dados corretos")
    @SuppressWarnings("unchecked")
    void notificarNovaMarcacao_DeveEnviarRequest() {
        LocalDateTime data = LocalDateTime.of(2026, 5, 20, 10, 30);
        service.notificarNovaMarcacao(1L, 100L, data);

        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = castCaptor(ArgumentCaptor.forClass(HttpEntity.class));
        verify(restTemplate).postForObject(anyString(), entityCaptor.capture(), eq(Void.class));

        HttpEntity<Map<String, Object>> entity = entityCaptor.getValue();
        Map<String, Object> body = entity.getBody();
        assertNotNull(body);
        assertEquals(1L, body.get("utilizadorId"));
        assertEquals("Marcação Criada", body.get("titulo"));

        Map<String, Object> metadata = (Map<String, Object>) body.get("metadata");
        assertNotNull(metadata);
        assertEquals("100", metadata.get("appointmentId"));
        assertEquals("CREATED", metadata.get("notificationSubtype"));
    }

    @Test
    @DisplayName("notificarLembreteUmDia deve enviar metadados corretos de lembrete")
    @SuppressWarnings("unchecked")
    void notificarLembreteUmDia_DeveEnviarRequest() {
        LocalDateTime data = LocalDateTime.of(2026, 5, 20, 10, 30);
        service.notificarLembreteUmDia(1L, 100L, data);

        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = castCaptor(ArgumentCaptor.forClass(HttpEntity.class));
        verify(restTemplate).postForObject(anyString(), entityCaptor.capture(), eq(Void.class));

        HttpEntity<Map<String, Object>> entity = entityCaptor.getValue();
        Map<String, Object> body = entity.getBody();
        assertNotNull(body);
        assertEquals(1L, body.get("utilizadorId"));

        Map<String, Object> metadata = (Map<String, Object>) body.get("metadata");
        assertNotNull(metadata);
        assertEquals("100", metadata.get("appointmentId"));
        assertEquals("REMINDER_1_DAY", metadata.get("notificationSubtype"));
    }

    @Test
    @DisplayName("notificarCancelamento deve conter motivo do cancelamento")
    @SuppressWarnings("unchecked")
    void notificarCancelamento_DeveEnviarRequest() {
        LocalDateTime data = LocalDateTime.of(2026, 5, 20, 10, 30);
        service.notificarCancelamento(1L, data, "Impossibilidade pessoal");

        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = castCaptor(ArgumentCaptor.forClass(HttpEntity.class));
        verify(restTemplate).postForObject(anyString(), entityCaptor.capture(), eq(Void.class));

        HttpEntity<Map<String, Object>> entity = entityCaptor.getValue();
        Map<String, Object> body = entity.getBody();
        assertNotNull(body);
        assertTrue(body.get("mensagem").toString().contains("Impossibilidade pessoal"));

        Map<String, Object> metadata = (Map<String, Object>) body.get("metadata");
        assertNotNull(metadata);
        assertEquals("CANCELLED", metadata.get("notificationSubtype"));
    }

    @Test
    @DisplayName("notificarCancelamento sem motivo deve assumir valor padrão")
    void notificarCancelamento_SemMotivo_DeveEnviarRequest() {
        LocalDateTime data = LocalDateTime.of(2026, 5, 20, 10, 30);
        service.notificarCancelamento(1L, data, null);

        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = castCaptor(ArgumentCaptor.forClass(HttpEntity.class));
        verify(restTemplate).postForObject(anyString(), entityCaptor.capture(), eq(Void.class));

        HttpEntity<Map<String, Object>> entity = entityCaptor.getValue();
        Map<String, Object> body = entity.getBody();
        assertNotNull(body);
        assertTrue(body.get("mensagem").toString().contains("sem motivo especificado"));
    }

    @Test
    @DisplayName("notificarCancelamentoPeloUtente deve mapear cancelamento iniciado pelo utente")
    void notificarCancelamentoPeloUtente_DeveEnviarRequest() {
        LocalDateTime data = LocalDateTime.of(2026, 5, 20, 10, 30);
        service.notificarCancelamentoPeloUtente(1L, "Manuel Silva", data);

        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = castCaptor(ArgumentCaptor.forClass(HttpEntity.class));
        verify(restTemplate).postForObject(anyString(), entityCaptor.capture(), eq(Void.class));

        HttpEntity<Map<String, Object>> entity = entityCaptor.getValue();
        Map<String, Object> body = entity.getBody();
        assertNotNull(body);
        assertTrue(body.get("mensagem").toString().contains("Manuel Silva"));
        assertEquals("CANCELAMENTO", body.get("tipo"));
    }

    @Test
    @DisplayName("notificarDocumentosInvalidos deve enviar alerta de documentos")
    void notificarDocumentosInvalidos_DeveEnviarRequest() {
        service.notificarDocumentosInvalidos(1L, "BI rasurado");

        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = castCaptor(ArgumentCaptor.forClass(HttpEntity.class));
        verify(restTemplate).postForObject(anyString(), entityCaptor.capture(), eq(Void.class));

        HttpEntity<Map<String, Object>> entity = entityCaptor.getValue();
        Map<String, Object> body = entity.getBody();
        assertNotNull(body);
        assertTrue(body.get("mensagem").toString().contains("BI rasurado"));
    }

    @Test
    @DisplayName("notificarReagendamentoPeloUtente deve enviar dados antigos e novos de agendamento")
    @SuppressWarnings("unchecked")
    void notificarReagendamentoPeloUtente_DeveEnviarRequest() {
        LocalDateTime dataAntiga = LocalDateTime.of(2026, 5, 20, 10, 30);
        LocalDateTime dataNova = LocalDateTime.of(2026, 5, 21, 14, 0);
        service.notificarReagendamentoPeloUtente(1L, "Manuel Silva", dataAntiga, dataNova);

        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = castCaptor(ArgumentCaptor.forClass(HttpEntity.class));
        verify(restTemplate).postForObject(anyString(), entityCaptor.capture(), eq(Void.class));

        HttpEntity<Map<String, Object>> entity = entityCaptor.getValue();
        Map<String, Object> body = entity.getBody();
        assertNotNull(body);

        Map<String, Object> metadata = (Map<String, Object>) body.get("metadata");
        assertNotNull(metadata);
        assertEquals("2026-05-20", metadata.get("oldDate"));
        assertEquals("2026-05-21", metadata.get("newDate"));
        assertEquals("RESCHEDULED", metadata.get("notificationSubtype"));
    }

    @Test
    @DisplayName("Erro no microsserviço externo não deve propagar exceção")
    void enviarParaMicrosservico_ComErro_NaoDeveFalhar() {
        doThrow(new RuntimeException("Conexão recusada")).when(restTemplate).postForObject(anyString(), any(), any());

        assertDoesNotThrow(() ->
            service.criarNotificacao(1L, "T", "M", "T")
        );
    }
}