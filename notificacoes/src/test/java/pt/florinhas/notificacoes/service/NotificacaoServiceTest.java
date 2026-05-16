package pt.florinhas.notificacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import pt.florinhas.common_data.domain.Notificacao;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.NotificacaoRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.notificacoes.service.email.EmailService;

class NotificacaoServiceTest {

        private NotificacaoRepository notificacaoRepository;

        private UtilizadorRepository utilizadorRepository;

        private EmailService emailService;

        private SimpMessagingTemplate messagingTemplate;

        private NotificacaoService service;

        @BeforeEach
        void setUp() {

                notificacaoRepository = mock(NotificacaoRepository.class);

                utilizadorRepository = mock(UtilizadorRepository.class);

                emailService = mock(EmailService.class);

                messagingTemplate = mock(SimpMessagingTemplate.class);

                service = new NotificacaoService(
                                notificacaoRepository,
                                utilizadorRepository,
                                emailService,
                                messagingTemplate);
        }

        private Utilizador createUser() {

                Utilizador u = new Utilizador();

                u.setId(1L);
                u.setEmail("teste@teste.com");
                u.setNif("123456789");

                return u;
        }

        @Test
        void criarNotificacao_DeveGuardar() {

                Utilizador user = createUser();

                when(utilizadorRepository.findById(1L))
                                .thenReturn(Optional.of(user));

                when(notificacaoRepository.save(any()))
                                .thenAnswer(i -> i.getArgument(0));

                Notificacao result = service.criarNotificacao(
                                1L,
                                "Titulo",
                                "Mensagem",
                                "INFO",
                                Map.of());

                assertNotNull(result);

                verify(notificacaoRepository)
                                .save(any());

                verify(messagingTemplate)
                                .convertAndSendToUser(
                                                anyString(),
                                                eq("/queue/notifications"),
                                                any());
        }

        @Test
        void contarNaoLidas_DeveRetornarValor() {

                when(notificacaoRepository
                                .countByUtilizadorIdAndLidaFalse(1L))
                                .thenReturn(5L);

                long result = service.contarNaoLidas(1L);

                assertEquals(5L, result);
        }

        @Test
        void marcarComoLida_DeveAtualizar() {

                Notificacao notificacao = new Notificacao();

                when(notificacaoRepository
                                .findByIdAndUtilizadorId(1L, 1L))
                                .thenReturn(Optional.of(notificacao));

                service.marcarComoLida(1L, 1L);

                assertTrue(notificacao.isLida());

                verify(notificacaoRepository)
                                .save(notificacao);
        }

        @Test
        void eliminarTodas_DeveExecutar() {

                service.eliminarTodas(1L);

                verify(notificacaoRepository)
                                .deleteByUtilizadorId(1L);
        }

        @Test
        void notificarNovaMarcacao_DeveEnviarEmail() {

                Utilizador user = createUser();

                when(utilizadorRepository.findById(1L))
                                .thenReturn(Optional.of(user));

                when(notificacaoRepository.save(any()))
                                .thenAnswer(i -> i.getArgument(0));

                service.notificarNovaMarcacao(
                                1L,
                                5L,
                                LocalDateTime.now(),
                                30,
                                "Consulta");

                verify(emailService)
                                .sendAppointmentCreated(
                                                anyString(),
                                                any(),
                                                eq(5L),
                                                eq("Consulta"),
                                                eq(30));
        }
}