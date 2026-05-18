package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class NotificacaoServiceTest {

        private NotificacaoService service;

        @BeforeEach
        void setup() {
                RestTemplate restTemplate = mock(RestTemplate.class);
                service = new NotificacaoService(restTemplate);
                service.setNotificacoesUrl("http://localhost:8083");
                service.setGatewaySecret("secret");
        }

        @Test
        void criarNotificacao_DeveExecutarSemErro() {

                assertDoesNotThrow(() -> service.criarNotificacao(
                                1L,
                                "Titulo",
                                "Mensagem",
                                "TIPO"));
        }

        @Test
        void notificarNovaMarcacaoParaSecretaria_DeveExecutarSemErro() {

                assertDoesNotThrow(() -> service.notificarNovaMarcacaoParaSecretaria(
                                1L,
                                "Joao",
                                10L,
                                LocalDateTime.now(),
                                "Consulta"));
        }

        @Test
        void notificarNovaMarcacao_DeveExecutarSemErro() {

                assertDoesNotThrow(() -> service.notificarNovaMarcacao(
                                1L,
                                10L,
                                LocalDateTime.now()));
        }

        @Test
        void notificarLembreteUmDia_DeveExecutarSemErro() {

                assertDoesNotThrow(() -> service.notificarLembreteUmDia(
                                1L,
                                10L,
                                LocalDateTime.now()));
        }

        @Test
        void notificarCancelamento_DeveExecutarSemErro() {

                assertDoesNotThrow(() -> service.notificarCancelamento(
                                1L,
                                LocalDateTime.now(),
                                "Motivo"));
        }

        @Test
        void notificarCancelamento_SemMotivo_DeveExecutarSemErro() {

                assertDoesNotThrow(() -> service.notificarCancelamento(
                                1L,
                                LocalDateTime.now(),
                                null));
        }

        @Test
        void notificarCancelamentoPeloUtente_DeveExecutarSemErro() {

                assertDoesNotThrow(() -> service.notificarCancelamentoPeloUtente(
                                1L,
                                "Joao",
                                LocalDateTime.now()));
        }

        @Test
        void notificarDocumentosInvalidos_DeveExecutarSemErro() {

                assertDoesNotThrow(() -> service.notificarDocumentosInvalidos(
                                1L,
                                "Documento ilegível"));
        }

        @Test
        void notificarReagendamentoPeloUtente_DeveExecutarSemErro() {

                assertDoesNotThrow(() -> service.notificarReagendamentoPeloUtente(
                                1L,
                                "Joao",
                                LocalDateTime.now(),
                                LocalDateTime.now().plusDays(1)));
        }
}