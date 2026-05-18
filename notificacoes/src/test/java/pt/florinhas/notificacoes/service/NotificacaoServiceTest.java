package pt.florinhas.notificacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import pt.florinhas.common_data.domain.Notificacao;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.dto.NotificacaoResponseDTO;
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
    @DisplayName("criarNotificacao deve guardar e enviar via websocket")
    void criarNotificacao_DeveGuardar() {
        Utilizador user = createUser();
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Notificacao result = service.criarNotificacao(1L, "Titulo", "Mensagem", "INFO", Map.of());

        assertNotNull(result);
        verify(notificacaoRepository).save(any());
        verify(messagingTemplate).convertAndSendToUser(
                eq("teste@teste.com"),
                eq("/queue/notifications"),
                any(NotificacaoResponseDTO.class));
    }

    @Test
    @DisplayName("criarNotificacao deve falhar com utilizador inexistente")
    void criarNotificacao_UtilizadorInexistente_DeveLancarExcecao() {
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> 
            service.criarNotificacao(1L, "T", "M", "T")
        );
    }

    @Test
    @DisplayName("criarNotificacao com WebSocket falhando não deve interromper transação")
    void criarNotificacao_WebSocketComErro_NaoDeveFalhar() {
        Utilizador user = createUser();
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("WebSocket down")).when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

        Notificacao result = assertDoesNotThrow(() ->
            service.criarNotificacao(1L, "Titulo", "Mensagem", "INFO", Map.of())
        );

        assertNotNull(result);
        verify(notificacaoRepository).save(any());
    }

    @Test
    @DisplayName("listarPorUtilizador deve retornar lista de DTOs convertidos")
    void listarPorUtilizador_DeveRetornarLista() {
        Notificacao n = new Notificacao();
        n.setId(10L);
        n.setTitulo("T");
        n.setMensagem("M");
        n.setLida(false);
        n.setUtilizador(createUser());

        when(notificacaoRepository.findByUtilizadorIdOrderByDataCriacaoDesc(1L)).thenReturn(List.of(n));

        List<NotificacaoResponseDTO> result = service.listarPorUtilizador(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals("T", result.get(0).getTitulo());
    }

    @Test
    @DisplayName("contarNaoLidas deve retornar quantidade correta")
    void contarNaoLidas_DeveRetornarValor() {
        when(notificacaoRepository.countByUtilizadorIdAndLidaFalse(1L)).thenReturn(5L);
        long result = service.contarNaoLidas(1L);
        assertEquals(5L, result);
    }

    @Test
    @DisplayName("marcarComoLida deve atualizar estado da notificação")
    void marcarComoLida_DeveAtualizar() {
        Notificacao notificacao = new Notificacao();
        notificacao.setLida(false);

        when(notificacaoRepository.findByIdAndUtilizadorId(1L, 1L)).thenReturn(Optional.of(notificacao));

        service.marcarComoLida(1L, 1L);

        assertTrue(notificacao.isLida());
        verify(notificacaoRepository).save(notificacao);
    }

    @Test
    @DisplayName("marcarComoLida deve falhar para notificação inexistente")
    void marcarComoLida_Inexistente_DeveLancarExcecao() {
        when(notificacaoRepository.findByIdAndUtilizadorId(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.marcarComoLida(1L, 1L));
    }

    @Test
    @DisplayName("marcarTodasComoLidas deve atualizar todas as notificações do utilizador")
    void marcarTodasComoLidas_DeveAtualizarTodas() {
        Notificacao n1 = new Notificacao();
        n1.setLida(false);
        Notificacao n2 = new Notificacao();
        n2.setLida(false);

        when(notificacaoRepository.findByUtilizadorIdOrderByDataCriacaoDesc(1L)).thenReturn(List.of(n1, n2));

        service.marcarTodasComoLidas(1L);

        assertTrue(n1.isLida());
        assertTrue(n2.isLida());
        verify(notificacaoRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("eliminarNotificacao deve apagar a notificação correspondente")
    void eliminarNotificacao_DeveApagar() {
        Notificacao notificacao = new Notificacao();
        when(notificacaoRepository.findByIdAndUtilizadorId(1L, 1L)).thenReturn(Optional.of(notificacao));

        service.eliminarNotificacao(1L, 1L);

        verify(notificacaoRepository).delete(notificacao);
    }

    @Test
    @DisplayName("eliminarNotificacao deve falhar com notificação inexistente")
    void eliminarNotificacao_Inexistente_DeveLancarExcecao() {
        when(notificacaoRepository.findByIdAndUtilizadorId(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.eliminarNotificacao(1L, 1L));
    }

    @Test
    @DisplayName("eliminarTodas deve invocar eliminação no repositório")
    void eliminarTodas_DeveExecutar() {
        service.eliminarTodas(1L);
        verify(notificacaoRepository).deleteByUtilizadorId(1L);
    }

    @Test
    @DisplayName("notificarNovaMarcacao deve salvar notificação e enviar email")
    void notificarNovaMarcacao_DeveEnviarEmail() {
        Utilizador user = createUser();
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.notificarNovaMarcacao(1L, 5L, LocalDateTime.now(), 30, "Consulta");

        verify(emailService).sendAppointmentCreated(anyString(), any(), eq(5L), eq("Consulta"), eq(30));
    }

    @Test
    @DisplayName("notificarLembreteUmDia deve salvar notificação e enviar email")
    void notificarLembreteUmDia_DeveSalvarEEnviarEmail() {
        Utilizador user = createUser();
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LocalDateTime data = LocalDateTime.now().plusDays(1);
        service.notificarLembreteUmDia(1L, 5L, data);

        verify(emailService).sendAppointmentReminderOneDay(eq("teste@teste.com"), eq(data));
        verify(notificacaoRepository).save(any());
    }

    @Test
    @DisplayName("notificarCancelamento deve salvar notificação e enviar email correspondente")
    void notificarCancelamento_DeveSalvarEEnviarEmail() {
        Utilizador user = createUser();
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LocalDateTime data = LocalDateTime.now();
        service.notificarCancelamento(1L, data, "Impossibilidade técnica");

        verify(emailService).sendAppointmentCancelled(eq("teste@teste.com"), eq("Impossibilidade técnica"));
        verify(notificacaoRepository).save(any());
    }

    @Test
    @DisplayName("notificarCancelamentoPeloUtente deve criar notificação de cancelamento")
    void notificarCancelamentoPeloUtente_DeveSalvar() {
        Utilizador user = createUser();
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LocalDateTime data = LocalDateTime.now();
        service.notificarCancelamentoPeloUtente(1L, "Manuel Silva", data);

        verify(notificacaoRepository).save(any());
    }

    @Test
    @DisplayName("notificarDocumentosInvalidos deve salvar notificação correspondente")
    void notificarDocumentosInvalidos_DeveSalvar() {
        Utilizador user = createUser();
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.notificarDocumentosInvalidos(1L, "Falta assinatura");

        verify(notificacaoRepository).save(any());
    }

    @Test
    @DisplayName("Falha no envio de email não deve falhar o fluxo principal")
    void sendEmailIfAvailable_ComErro_NaoDeveFalhar() {
        Utilizador user = createUser();
        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("SMTP down")).when(emailService).sendAppointmentReminderOneDay(anyString(), any());

        assertDoesNotThrow(() ->
            service.notificarLembreteUmDia(1L, 5L, LocalDateTime.now())
        );
    }
}