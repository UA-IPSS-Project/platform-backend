package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class NotificacaoServiceTest {

    private NotificacaoService service;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() throws Exception {

        service = new NotificacaoService();

        restTemplate = mock(RestTemplate.class);

        setField("restTemplate", restTemplate);
        setField("notificacoesUrl", "http://localhost");
        setField("gatewaySecret", "secret");
    }

    @Test
    void criarNotificacao_DeveEnviar() {

        service.criarNotificacao(1L, "Titulo", "Mensagem", "TIPO");

        verify(restTemplate).postForObject(
                contains("/api/internal/notificacoes/criar"),
                any(),
                any());
    }

    @Test
    void notificarNovaMarcacaoParaSecretaria_DeveEnviar() {

        service.notificarNovaMarcacaoParaSecretaria(
                1L,
                "Nuno",
                10L,
                LocalDateTime.now(),
                "Consulta");

        verify(restTemplate).postForObject(
                contains("/api/internal/notificacoes/criar"),
                any(),
                any());
    }

    @Test
    void notificarNovaMarcacao_DeveEnviar() {

        service.notificarNovaMarcacao(
                1L,
                10L,
                LocalDateTime.now(),
                30,
                "Consulta");

        verify(restTemplate).postForObject(
                contains("/api/internal/notificacoes/criar"),
                any(),
                any());
    }

    @Test
    void notificarLembreteUmDia_DeveEnviar() {

        service.notificarLembreteUmDia(
                1L,
                10L,
                LocalDateTime.now());

        verify(restTemplate).postForObject(
                contains("/api/internal/notificacoes/criar"),
                any(),
                any());
    }

    @Test
    void notificarCancelamento_DeveEnviar() {

        service.notificarCancelamento(
                1L,
                LocalDateTime.now(),
                "Motivo");

        verify(restTemplate).postForObject(
                contains("/api/internal/notificacoes/criar"),
                any(),
                any());
    }

    @Test
    void notificarCancelamentoPeloUtente_DeveEnviar() {

        service.notificarCancelamentoPeloUtente(
                1L,
                "Nuno",
                LocalDateTime.now());

        verify(restTemplate).postForObject(
                contains("/api/internal/notificacoes/criar"),
                any(),
                any());
    }

    @Test
    void notificarDocumentosInvalidos_DeveEnviar() {

        service.notificarDocumentosInvalidos(
                1L,
                "Inválido");

        verify(restTemplate).postForObject(
                contains("/api/internal/notificacoes/criar"),
                any(),
                any());
    }

    @Test
    void notificarReagendamentoPeloUtente_DeveEnviar() {

        service.notificarReagendamentoPeloUtente(
                1L,
                "Nuno",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1));

        verify(restTemplate).postForObject(
                contains("/api/internal/notificacoes/criar"),
                any(),
                any());
    }

    @Test
    void criarNotificacao_NaoDeveLancarErroQuandoFalha() {

        when(restTemplate.postForObject(
                any(),
                any(),
                any()))
                .thenThrow(new RuntimeException());

        assertDoesNotThrow(() ->
                service.criarNotificacao(
                        1L,
                        "Titulo",
                        "Mensagem",
                        "TIPO"));
    }

    private void setField(String fieldName, Object value) throws Exception {

        Field field =
                NotificacaoService.class.getDeclaredField(fieldName);

        field.setAccessible(true);
        field.set(service, value);
    }
}