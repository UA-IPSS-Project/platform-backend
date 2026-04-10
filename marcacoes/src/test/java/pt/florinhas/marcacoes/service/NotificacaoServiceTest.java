package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import pt.florinhas.common_data.domain.Notificacao;
import pt.florinhas.common_data.domain.NotificacaoTipo;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.dto.NotificacaoResponseDTO;
import pt.florinhas.common_data.repository.NotificacaoRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoSecretaria;
import pt.florinhas.marcacoes.exception.NotFoundException;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.service.email.EmailService;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private UtilizadorRepository utilizadorRepository;

    @Mock
    private MarcacaoRepository marcacaoRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificacaoService service;

    // =========================
    // HELPERS
    // =========================

    private Utilizador buildUser(Long id, String email, String nif) {
        Utilizador u = new Utente();
        u.setId(id);
        u.setEmail(email);
        u.setNif(nif);
        u.setNome("User " + id);
        return u;
    }

    private Notificacao buildNotificacao(Utilizador user) {
        Notificacao n = new Notificacao();
        n.setId(1L);
        n.setTitulo("Titulo");
        n.setMensagem("Mensagem");
        n.setTipo(NotificacaoTipo.LEMBRETE);
        n.setLida(false);
        n.setDataCriacao(LocalDateTime.now());
        n.setUtilizador(user);
        n.setMetadata(Map.of("k", "v"));
        return n;
    }

    // =========================
    // CRIAR NOTIFICACAO
    // =========================

    @Test
    void criarNotificacao_ComSucesso() {
        Utilizador user = buildUser(1L, "test@test.com", "100000002");

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> {
            Notificacao n = invocation.getArgument(0);
            n.setId(10L);
            n.setUtilizador(user);
            n.setDataCriacao(LocalDateTime.now());
            return n;
        });

        var result = service.criarNotificacao(1L, "Titulo", "Msg", NotificacaoTipo.LEMBRETE);

        assertNotNull(result);
        assertEquals("Titulo", result.getTitulo());
        assertEquals("Msg", result.getMensagem());
        assertFalse(result.isLida());

        verify(messagingTemplate).convertAndSendToUser(
                eq("test@test.com"),
                eq("/queue/notifications"),
                any(NotificacaoResponseDTO.class)
        );
    }

    @Test
    void criarNotificacao_DeveUsarNifQuandoEmailVazio() {
        Utilizador user = buildUser(1L, "   ", "100000002");

        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> {
            Notificacao n = invocation.getArgument(0);
            n.setId(10L);
            n.setUtilizador(user);
            n.setDataCriacao(LocalDateTime.now());
            return n;
        });

        var result = service.criarNotificacao(user, "Titulo", "Msg", NotificacaoTipo.LEMBRETE, null);

        assertNotNull(result);

        verify(messagingTemplate).convertAndSendToUser(
                eq("100000002"),
                eq("/queue/notifications"),
                any(NotificacaoResponseDTO.class)
        );
    }

    @Test
    void criarNotificacao_NaoDeveFalharQuandoWebsocketFalha() {
        Utilizador user = buildUser(1L, "test@test.com", "100000002");

        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> {
            Notificacao n = invocation.getArgument(0);
            n.setId(10L);
            n.setUtilizador(user);
            n.setDataCriacao(LocalDateTime.now());
            return n;
        });

        doThrow(new RuntimeException("ws error"))
                .when(messagingTemplate)
                .convertAndSendToUser(anyString(), anyString(), any());

        assertDoesNotThrow(() ->
                service.criarNotificacao(user, "Titulo", "Msg", NotificacaoTipo.LEMBRETE, null)
        );

        verify(notificacaoRepository).save(any(Notificacao.class));
    }

    @Test
    void criarNotificacao_DeveFalhar_QuandoUserNaoExiste() {
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.criarNotificacao(1L, "T", "M", NotificacaoTipo.LEMBRETE));
    }

    // =========================
    // LISTAR E CONTAR
    // =========================

    @Test
    void listarPorUtilizador_DeveRetornarLista() {
        Utilizador u = buildUser(1L, "a@a.com", "100000002");
        Notificacao n = buildNotificacao(u);

        when(notificacaoRepository.findByUtilizadorIdOrderByDataCriacaoDesc(1L))
                .thenReturn(List.of(n));

        var result = service.listarPorUtilizador(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUtilizadorId());
        assertEquals("Titulo", result.get(0).getTitulo());
        assertEquals("Mensagem", result.get(0).getMensagem());
    }

    @Test
    void contarNaoLidas_DeveRetornarValor() {
        when(notificacaoRepository.countByUtilizadorIdAndLidaFalse(1L))
                .thenReturn(5L);

        assertEquals(5L, service.contarNaoLidas(1L));
    }

    // =========================
    // MARCAR COMO LIDA
    // =========================

    @Test
    void marcarComoLida_ComSucesso() {
        Utilizador u = buildUser(2L, "a@a.com", "100000002");
        Notificacao n = buildNotificacao(u);

        when(notificacaoRepository.findByIdAndUtilizadorId(1L, 2L))
                .thenReturn(Optional.of(n));

        service.marcarComoLida(1L, 2L);

        assertTrue(n.isLida());
        verify(notificacaoRepository).save(n);
    }

    @Test
    void marcarComoLida_DeveFalhar_QuandoNaoExiste() {
        when(notificacaoRepository.findByIdAndUtilizadorId(1L, 2L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.marcarComoLida(1L, 2L));
    }

    // =========================
    // MARCAR TODAS
    // =========================

    @Test
    void marcarTodasComoLidas_ComSucesso() {
        Utilizador u = buildUser(1L, "a@a.com", "100000002");
        Notificacao n1 = buildNotificacao(u);
        Notificacao n2 = buildNotificacao(u);
        n2.setId(2L);

        when(notificacaoRepository.findByUtilizadorIdOrderByDataCriacaoDesc(1L))
                .thenReturn(List.of(n1, n2));

        service.marcarTodasComoLidas(1L);

        assertTrue(n1.isLida());
        assertTrue(n2.isLida());
        verify(notificacaoRepository).saveAll(List.of(n1, n2));
    }

    // =========================
    // ELIMINAR
    // =========================

    @Test
    void eliminarNotificacao_ComSucesso() {
        Utilizador u = buildUser(2L, "a@a.com", "100000002");
        Notificacao n = buildNotificacao(u);

        when(notificacaoRepository.findByIdAndUtilizadorId(1L, 2L))
                .thenReturn(Optional.of(n));

        service.eliminarNotificacao(1L, 2L);

        verify(notificacaoRepository).delete(n);
    }

    @Test
    void eliminarNotificacao_DeveFalhar_QuandoNaoExiste() {
        when(notificacaoRepository.findByIdAndUtilizadorId(1L, 2L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.eliminarNotificacao(1L, 2L));
    }

    @Test
    void eliminarTodas_ComSucesso() {
        service.eliminarTodas(1L);

        verify(notificacaoRepository).deleteByUtilizadorId(1L);
    }

    // =========================
    // NOTIFICAR NOVA MARCACAO
    // =========================

    @Test
    void notificarNovaMarcacao_DeveCriarNotificacaoEEnviarEmail() {
        Utilizador u = buildUser(1L, "a@a.com", "100000002");

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(u));
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> {
            Notificacao n = invocation.getArgument(0);
            n.setId(10L);
            n.setUtilizador(u);
            n.setDataCriacao(LocalDateTime.now());
            return n;
        });

        service.notificarNovaMarcacao(1L, 10L, LocalDateTime.now(), 15, "Consulta");

        verify(notificacaoRepository).save(any(Notificacao.class));
        verify(emailService).sendAppointmentCreated(
                anyString(),
                any(LocalDateTime.class),
                anyLong(),
                anyString(),
                anyInt()
        );
    }

    @Test
    void notificarNovaMarcacao_NaoDeveEnviarEmailQuandoNaoHaEmail() {
        Utilizador u = buildUser(1L, null, "100000002");

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(u));
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> {
            Notificacao n = invocation.getArgument(0);
            n.setId(10L);
            n.setUtilizador(u);
            n.setDataCriacao(LocalDateTime.now());
            return n;
        });

        service.notificarNovaMarcacao(1L, 10L, LocalDateTime.now(), 15, "Consulta");

        verify(notificacaoRepository).save(any(Notificacao.class));
        verify(emailService, never()).sendAppointmentCreated(anyString(), any(), anyLong(), anyString(), anyInt());
    }

    // =========================
    // CANCELAMENTO
    // =========================

    @Test
    void notificarCancelamento_DeveEnviarEmail() {
        Utilizador u = buildUser(1L, "a@a.com", "100000002");

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(u));
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> {
            Notificacao n = invocation.getArgument(0);
            n.setId(11L);
            n.setUtilizador(u);
            n.setDataCriacao(LocalDateTime.now());
            return n;
        });

        service.notificarCancelamento(1L, LocalDateTime.now(), "Motivo");

        verify(emailService).sendAppointmentCancelled(eq("a@a.com"), eq("Motivo"));
    }

    @Test
    void notificarCancelamento_DeveUsarTextoDefaultQuandoMotivoVazio() {
        Utilizador u = buildUser(1L, "a@a.com", "100000002");

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(u));
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> {
            Notificacao n = invocation.getArgument(0);
            n.setId(11L);
            n.setUtilizador(u);
            n.setDataCriacao(LocalDateTime.now());
            return n;
        });

        service.notificarCancelamento(1L, LocalDateTime.now(), " ");

        verify(emailService).sendAppointmentCancelled(eq("a@a.com"), eq("sem motivo especificado"));
    }

    @Test
    void notificarCancelamentoPeloUtente_DeveCriarNotificacao() {
        Utilizador u = buildUser(1L, "a@a.com", "100000002");

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(u));
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> {
            Notificacao n = invocation.getArgument(0);
            n.setId(12L);
            n.setUtilizador(u);
            n.setDataCriacao(LocalDateTime.now());
            return n;
        });

        service.notificarCancelamentoPeloUtente(1L, "Joao", LocalDateTime.now());

        verify(notificacaoRepository).save(any(Notificacao.class));
    }

    // =========================
    // DOCUMENTOS INVALIDOS
    // =========================

    @Test
    void notificarDocumentosInvalidos_DeveCriarNotificacao() {
        Utilizador u = buildUser(1L, "a@a.com", "100000002");

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(u));
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> {
            Notificacao n = invocation.getArgument(0);
            n.setId(13L);
            n.setUtilizador(u);
            n.setDataCriacao(LocalDateTime.now());
            return n;
        });

        service.notificarDocumentosInvalidos(1L, "Erro");

        verify(notificacaoRepository).save(any(Notificacao.class));
    }

    // =========================
    // REMINDER 1 DIA
    // =========================

    @Test
    void notificarMarcacoesEmUmDia_DeveCriarNotificacaoSeNaoExiste() {
        Utente u = new Utente();
        u.setId(1L);
        u.setEmail("a@a.com");
        u.setNif("100000002");
        u.setNome("Utente");

        MarcacaoSecretaria sec = new MarcacaoSecretaria();
        sec.setUtente(u);

        Marcacao m = new Marcacao();
        m.setId(10L);
        m.setEstado(EventoEstado.AGENDADO);
        m.setData(LocalDateTime.now().plusDays(1));
        m.setMarcacaoSecretaria(sec);

        when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("SECRETARIA")))
                .thenReturn(List.of(m));

        when(notificacaoRepository.existsByUtilizadorIdAndTituloAndMensagemAndTipo(anyLong(), anyString(), anyString(), any()))
                .thenReturn(false);

        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> {
            Notificacao n = invocation.getArgument(0);
            n.setId(20L);
            n.setUtilizador(u);
            n.setDataCriacao(LocalDateTime.now());
            return n;
        });

        service.notificarMarcacoesEmUmDia();

        verify(notificacaoRepository).save(any(Notificacao.class));
        verify(emailService).sendAppointmentReminderOneDay(eq("a@a.com"), any(LocalDateTime.class));
    }

    @Test
    void notificarMarcacoesEmUmDia_NaoDeveCriarDuplicado() {
        Utente u = new Utente();
        u.setId(1L);
        u.setEmail("a@a.com");
        u.setNif("100000002");

        MarcacaoSecretaria sec = new MarcacaoSecretaria();
        sec.setUtente(u);

        Marcacao m = new Marcacao();
        m.setId(10L);
        m.setEstado(EventoEstado.AGENDADO);
        m.setData(LocalDateTime.now().plusDays(1));
        m.setMarcacaoSecretaria(sec);

        when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("SECRETARIA")))
                .thenReturn(List.of(m));

        when(notificacaoRepository.existsByUtilizadorIdAndTituloAndMensagemAndTipo(anyLong(), anyString(), anyString(), any()))
                .thenReturn(true);

        service.notificarMarcacoesEmUmDia();

        verify(notificacaoRepository, never()).save(any(Notificacao.class));
        verify(emailService, never()).sendAppointmentReminderOneDay(anyString(), any(LocalDateTime.class));
    }

    @Test
    void notificarMarcacoesEmUmDia_DeveIgnorarMarcacoesNaoAgendadas() {
        Utente u = new Utente();
        u.setId(1L);

        MarcacaoSecretaria sec = new MarcacaoSecretaria();
        sec.setUtente(u);

        Marcacao m = new Marcacao();
        m.setId(10L);
        m.setEstado(EventoEstado.CANCELADO);
        m.setData(LocalDateTime.now().plusDays(1));
        m.setMarcacaoSecretaria(sec);

        when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), eq("SECRETARIA")))
                .thenReturn(List.of(m));

        service.notificarMarcacoesEmUmDia();

        verify(notificacaoRepository, never()).save(any(Notificacao.class));
    }
}