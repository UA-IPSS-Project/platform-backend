package pt.florinhas.notificacoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import pt.florinhas.common_data.domain.Notificacao;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.dto.NotificacaoResponseDTO;
import pt.florinhas.common_data.repository.NotificacaoRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;

class NotificacaoServiceTest {

    private NotificacaoRepository notificacaoRepository;
    private UtilizadorRepository utilizadorRepository;
    private SimpMessagingTemplate messagingTemplate;

    private NotificacaoService service;

    @BeforeEach
    void setUp() {

        notificacaoRepository =
                mock(NotificacaoRepository.class);

        utilizadorRepository =
                mock(UtilizadorRepository.class);

        messagingTemplate =
                mock(SimpMessagingTemplate.class);

        service =
                new NotificacaoService(
                        notificacaoRepository,
                        utilizadorRepository,
                        messagingTemplate);
    }

    @Test
    void criarNotificacao_DeveCriar() {

        Utilizador user =
                new Utilizador();

        user.setId(1L);
        user.setEmail("teste@test.com");

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(notificacaoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        Notificacao result =
                service.criarNotificacao(
                        1L,
                        "Titulo",
                        "Mensagem",
                        "INFO");

        assertEquals(
                "Titulo",
                result.getTitulo());

        verify(messagingTemplate)
                .convertAndSendToUser(
                        any(),
                        any(),
                        any());
    }

    @Test
    void criarNotificacao_DeveIgnorarErroWebsocket() {

        Utilizador user =
                new Utilizador();

        user.setId(1L);
        user.setEmail("teste@test.com");

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(notificacaoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        doThrow(new RuntimeException())
                .when(messagingTemplate)
                .convertAndSendToUser(
                        any(),
                        any(),
                        any());

        Notificacao result =
                service.criarNotificacao(
                        1L,
                        "Titulo",
                        "Mensagem",
                        "INFO");

        assertNotNull(result);
    }

    @Test
    void listarPorUtilizador_DeveRetornarLista() {

        Utilizador user =
                new Utilizador();

        user.setId(1L);

        Notificacao notificacao =
                new Notificacao();

        notificacao.setId(2L);
        notificacao.setUtilizador(user);

        when(notificacaoRepository
                .findByUtilizadorIdOrderByDataCriacaoDesc(
                        1L))
                .thenReturn(List.of(notificacao));

        List<NotificacaoResponseDTO> result =
                service.listarPorUtilizador(1L);

        assertEquals(
                1,
                result.size());
    }

    @Test
    void contarNaoLidas_DeveRetornarValor() {

        when(notificacaoRepository
                .countByUtilizadorIdAndLidaFalse(
                        1L))
                .thenReturn(5L);

        assertEquals(
                5L,
                service.contarNaoLidas(1L));
    }

    @Test
    void marcarComoLida_DeveAtualizar() {

        Notificacao notificacao =
                new Notificacao();

        when(notificacaoRepository.findByIdAndUtilizadorId(
                1L,
                2L))
                .thenReturn(Optional.of(notificacao));

        service.marcarComoLida(
                1L,
                2L);

        assertEquals(
                true,
                notificacao.isLida());

        verify(notificacaoRepository)
                .save(notificacao);
    }

    @Test
    void marcarTodasComoLidas_DeveAtualizarTodas() {

        Notificacao notificacao =
                new Notificacao();

        when(notificacaoRepository
                .findByUtilizadorIdOrderByDataCriacaoDesc(
                        1L))
                .thenReturn(List.of(notificacao));

        service.marcarTodasComoLidas(1L);

        assertEquals(
                true,
                notificacao.isLida());

        verify(notificacaoRepository)
                .saveAll(any());
    }

    @Test
    void eliminarNotificacao_DeveEliminar() {

        Notificacao notificacao =
                new Notificacao();

        when(notificacaoRepository.findByIdAndUtilizadorId(
                1L,
                2L))
                .thenReturn(Optional.of(notificacao));

        service.eliminarNotificacao(
                1L,
                2L);

        verify(notificacaoRepository)
                .delete(notificacao);
    }

    @Test
    void eliminarTodas_DeveEliminar() {

        service.eliminarTodas(1L);

        verify(notificacaoRepository)
                .deleteByUtilizadorId(1L);
    }

    @Test
    void notificarNovaMarcacao_DeveCriar() {

        Utilizador user =
                new Utilizador();

        user.setId(1L);
        user.setEmail("teste@test.com");

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(notificacaoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        service.notificarNovaMarcacao(
                1L,
                2L,
                LocalDateTime.now(),
                15,
                "Consulta");

        verify(notificacaoRepository)
                .save(any());
    }

    @Test
    void notificarLembreteUmDia_DeveCriar() {

        Utilizador user =
                new Utilizador();

        user.setId(1L);
        user.setEmail("teste@test.com");

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(notificacaoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        service.notificarLembreteUmDia(
                1L,
                2L,
                LocalDateTime.now());

        verify(notificacaoRepository)
                .save(any());
    }

    @Test
    void notificarCancelamento_DeveCriar() {

        Utilizador user =
                new Utilizador();

        user.setId(1L);
        user.setEmail("teste@test.com");

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(notificacaoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        service.notificarCancelamento(
                1L,
                LocalDateTime.now(),
                "Motivo");

        verify(notificacaoRepository)
                .save(any());
    }

    @Test
    void notificarCancelamentoPeloUtente_DeveCriar() {

        Utilizador user =
                new Utilizador();

        user.setId(1L);
        user.setEmail("teste@test.com");

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(notificacaoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        service.notificarCancelamentoPeloUtente(
                1L,
                "Nuno",
                LocalDateTime.now());

        verify(notificacaoRepository)
                .save(any());
    }

    @Test
    void notificarDocumentosInvalidos_DeveCriar() {

        Utilizador user =
                new Utilizador();

        user.setId(1L);
        user.setEmail("teste@test.com");

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(notificacaoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        service.notificarDocumentosInvalidos(
                1L,
                "Obs");

        verify(notificacaoRepository)
                .save(any());
    }
}