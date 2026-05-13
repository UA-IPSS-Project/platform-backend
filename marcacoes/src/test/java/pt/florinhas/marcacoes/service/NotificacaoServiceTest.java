package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class NotificacaoServiceTest {

    private NotificacaoService service;

    @BeforeEach
    void setup() throws Exception {

        service = new NotificacaoService();

        setField(
                service,
                "notificacoesUrl",
                "http://localhost:8083"
        );

        setField(
                service,
                "gatewaySecret",
                "secret"
        );

        RestTemplate restTemplate =
                org.mockito.Mockito.mock(RestTemplate.class);

        Field field =
                NotificacaoService.class
                        .getDeclaredField("restTemplate");

        field.setAccessible(true);
        field.set(service, restTemplate);
    }

    @Test
    void criarNotificacao_DeveExecutarSemErro() {

        assertDoesNotThrow(() ->
                service.criarNotificacao(
                        1L,
                        "Titulo",
                        "Mensagem",
                        "TIPO"
                )
        );
    }

    @Test
    void notificarNovaMarcacaoParaSecretaria_DeveExecutarSemErro() {

        assertDoesNotThrow(() ->
                service.notificarNovaMarcacaoParaSecretaria(
                        1L,
                        "Joao",
                        10L,
                        LocalDateTime.now(),
                        "Consulta"
                )
        );
    }

    @Test
    void notificarNovaMarcacao_DeveExecutarSemErro() {

        assertDoesNotThrow(() ->
                service.notificarNovaMarcacao(
                        1L,
                        10L,
                        LocalDateTime.now(),
                        30,
                        "Resumo"
                )
        );
    }

    @Test
    void notificarLembreteUmDia_DeveExecutarSemErro() {

        assertDoesNotThrow(() ->
                service.notificarLembreteUmDia(
                        1L,
                        10L,
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void notificarCancelamento_DeveExecutarSemErro() {

        assertDoesNotThrow(() ->
                service.notificarCancelamento(
                        1L,
                        LocalDateTime.now(),
                        "Motivo"
                )
        );
    }

    @Test
    void notificarCancelamento_SemMotivo_DeveExecutarSemErro() {

        assertDoesNotThrow(() ->
                service.notificarCancelamento(
                        1L,
                        LocalDateTime.now(),
                        null
                )
        );
    }

    @Test
    void notificarCancelamentoPeloUtente_DeveExecutarSemErro() {

        assertDoesNotThrow(() ->
                service.notificarCancelamentoPeloUtente(
                        1L,
                        "Joao",
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void notificarDocumentosInvalidos_DeveExecutarSemErro() {

        assertDoesNotThrow(() ->
                service.notificarDocumentosInvalidos(
                        1L,
                        "Documento ilegível"
                )
        );
    }

    @Test
    void notificarReagendamentoPeloUtente_DeveExecutarSemErro() {

        assertDoesNotThrow(() ->
                service.notificarReagendamentoPeloUtente(
                        1L,
                        "Joao",
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1)
                )
        );
    }

    private void setField(
            Object target,
            String fieldName,
            Object value
    ) throws Exception {

        Field field =
                target.getClass()
                        .getDeclaredField(fieldName);

        field.setAccessible(true);
        field.set(target, value);
    }
}