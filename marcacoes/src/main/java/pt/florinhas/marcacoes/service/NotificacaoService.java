package pt.florinhas.marcacoes.service;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;
import pt.florinhas.marcacoes.domain.Notificacao;
import pt.florinhas.marcacoes.domain.NotificacaoTipo;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.NotificacaoResponseDTO;
import pt.florinhas.marcacoes.exception.NotFoundException;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.repository.NotificacaoRepository;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.service.email.EmailService;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'as' HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String ONE_DAY_REMINDER_TITLE = "Lembrete de Marcacao";
    private static final String METADATA_SUBTYPE_KEY = "notificationSubtype";

    private final NotificacaoRepository notificacaoRepository;
    private final UtilizadorRepository utilizadorRepository;
    private final MarcacaoRepository marcacaoRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    public Notificacao criarNotificacao(Long utilizadorId, String titulo, String mensagem, NotificacaoTipo tipo) {
        return criarNotificacao(utilizadorId, titulo, mensagem, tipo, null);
    }

    public Notificacao criarNotificacao(Long utilizadorId, String titulo, String mensagem, NotificacaoTipo tipo,
            Map<String, Object> metadata) {
        Utilizador user = utilizadorRepository.findById(utilizadorId)
                .orElseThrow(() -> new NotFoundException("Utilizador não encontrado"));

        Notificacao notificacao = new Notificacao();
        notificacao.setUtilizador(user);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacao.setTipo(tipo);
        notificacao.setLida(false);
        notificacao.setMetadata(metadata);

        Notificacao saved = notificacaoRepository.save(notificacao);

        // Send real-time notification
        try {
            NotificacaoResponseDTO dto = converterParaDTO(saved);
                logger.info(
                    "Sending WebSocket notification to user: {} (email: {}), title: {}",
                    utilizadorId, user.getEmail(), titulo);
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(), // Assuming UserDetails username is email, we need to make sure this matches
                                     // what principal.getName() returns.
                    // Actually, in SecurityConfig/JwtAuthenticationFilter, the principal is
                    // UserDetails.
                    // The "user" in convertAndSendToUser is matched against Principal.getName().
                    // If UserDetails.getUsername() returns email, then this is correct.
                    "/queue/notifications",
                    dto);
                logger.info(
                    "WebSocket notification sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            // Log but don't fail transaction
                logger.error("Failed to send websocket notification", e);
        }

        return saved;
    }

    public List<NotificacaoResponseDTO> listarPorUtilizador(Long utilizadorId) {
        return notificacaoRepository.findByUtilizadorIdOrderByDataCriacaoDesc(utilizadorId).stream()
                .map(this::converterParaDTO)
                .toList();
    }

    private NotificacaoResponseDTO converterParaDTO(Notificacao n) {
        NotificacaoResponseDTO dto = new NotificacaoResponseDTO();
        dto.setId(n.getId());
        dto.setTitulo(n.getTitulo());
        dto.setMensagem(n.getMensagem());
        dto.setTipo(n.getTipo());
        dto.setLida(n.isLida());
        dto.setDataCriacao(n.getDataCriacao());
        dto.setUtilizadorId(n.getUtilizador().getId());
        dto.setMetadata(n.getMetadata());
        return dto;
    }

    public long contarNaoLidas(Long utilizadorId) {
        return notificacaoRepository.countByUtilizadorIdAndLidaFalse(utilizadorId);
    }

    @Transactional
    public void marcarComoLida(Long id, Long utilizadorId) {
        Notificacao notificacao = notificacaoRepository.findByIdAndUtilizadorId(id, utilizadorId)
                .orElseThrow(() -> new NotFoundException("Notificação não encontrada"));

        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
    }

    @Transactional
    public void marcarTodasComoLidas(Long utilizadorId) {
        List<Notificacao> notificacoes = notificacaoRepository.findByUtilizadorIdOrderByDataCriacaoDesc(utilizadorId);
        notificacoes.forEach(n -> n.setLida(true));
        notificacaoRepository.saveAll(notificacoes);
    }

    @Transactional
    public void eliminarNotificacao(Long id, Long utilizadorId) {
        Notificacao notificacao = notificacaoRepository.findByIdAndUtilizadorId(id, utilizadorId)
                .orElseThrow(() -> new NotFoundException("Notificação não encontrada"));
        notificacaoRepository.delete(notificacao);
    }

    @Transactional
    public void eliminarTodas(Long utilizadorId) {
        notificacaoRepository.deleteByUtilizadorId(utilizadorId);
    }

    // --- Métodos de Negócio (Semantic Methods) ---

    @Transactional
    public void notificarNovaMarcacao(Utilizador utilizador, Long marcacaoId, LocalDateTime data, boolean isRemote) {
        String dataFormatada = data.format(DISPLAY_DATE_FORMATTER);
        String mensagem = "Marcacao criada para " + dataFormatada + ".";
        String assunto = "Marcacao Criada";

        Map<String, Object> metadata = Map.of(
            "appointmentId", marcacaoId.toString(),
            "createdDate", data.format(DATE_FORMATTER),
            "createdTime", data.format(TIME_FORMATTER),
            METADATA_SUBTYPE_KEY, "CREATED");
        criarNotificacao(utilizador.getId(), assunto, mensagem, NotificacaoTipo.LEMBRETE, metadata);
        sendEmailIfAvailable(utilizador.getEmail(), () -> emailService.sendAppointmentCreated(utilizador.getEmail(), data));
    }

    @Transactional
        public void notificarCancelamento(Utilizador utilizador, LocalDateTime data, String motivo) {
        String assunto = "Marcacao Cancelada";
        String motivoTexto = (motivo == null || motivo.isBlank()) ? "sem motivo especificado" : motivo;
        String mensagem = "Marcacao cancelada por " + motivoTexto + ".";

        Map<String, Object> metadata = Map.of(
                "cancelledDate", data.format(DATE_FORMATTER),
            "cancelledTime", data.format(TIME_FORMATTER),
            METADATA_SUBTYPE_KEY, "CANCELLED");

        criarNotificacao(utilizador.getId(), assunto, mensagem, NotificacaoTipo.CANCELAMENTO, metadata);
        sendEmailIfAvailable(utilizador.getEmail(), () -> emailService.sendAppointmentCancelled(utilizador.getEmail(), motivoTexto));
    }

    @Transactional
    public void notificarCancelamentoPeloUtente(Utilizador destinatario, String nomeUtente,
            LocalDateTime data) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("dd/MM/yyyy 'às' HH:mm");
        String dataFormatada = data.format(formatter);

        String mensagem = "O utente " + nomeUtente + " cancelou a marcação de " + dataFormatada;
        String assunto = "Marcação Cancelada pelo Utente";

        // Adicionar metadata com data e hora do slot cancelado
        Map<String, Object> metadata = Map.of(
            "cancelledDate", data.format(DATE_FORMATTER),
            "cancelledTime", data.format(TIME_FORMATTER));

        criarNotificacao(destinatario.getId(), assunto, mensagem, NotificacaoTipo.CANCELAMENTO, metadata);
        // Admin notifications might not need email simulation, but keeping consistent
    }

    @Transactional
    public void notificarDocumentosInvalidos(Utilizador utilizador, String observacoes) {
        String mensagem = "Os documentos apresentados são inválidos. Por favor, contacte a secretaria. Observações: " + observacoes;
        String assunto = "Documentos Inválidos";

        criarNotificacao(utilizador.getId(), assunto, mensagem, NotificacaoTipo.LEMBRETE); // Using LEMBRETE as
                                                                                           // warning/info
    }

    @Scheduled(cron = "0 0 8 * * *") // Every day at 08:00
    @Transactional
    public void notificarMarcacoesEmUmDia() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.toLocalDate().plusDays(1).atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);

        List<Marcacao> marcacoes = marcacaoRepository.findMarcacoesBetweenDates(start, end, "SECRETARIA");

        for (Marcacao marcacao : marcacoes) {
            if (marcacao.getEstado() == EventoEstado.AGENDADO
                && marcacao.getMarcacaoSecretaria() != null
                && marcacao.getMarcacaoSecretaria().getUtente() != null) {
            Utilizador utente = marcacao.getMarcacaoSecretaria().getUtente();
            String dataFormatada = marcacao.getData().format(DISPLAY_DATE_FORMATTER);
            String mensagem = "Marcacao em 1 dia (" + dataFormatada + ").";

            boolean jaNotificado = notificacaoRepository.existsByUtilizadorIdAndTituloAndMensagemAndTipo(
                utente.getId(),
                ONE_DAY_REMINDER_TITLE,
                mensagem,
                NotificacaoTipo.LEMBRETE);

            if (!jaNotificado) {
                Map<String, Object> metadata = Map.of(
                    "appointmentId", String.valueOf(marcacao.getId()),
                    METADATA_SUBTYPE_KEY, "REMINDER_1_DAY");

                criarNotificacao(utente.getId(), ONE_DAY_REMINDER_TITLE, mensagem, NotificacaoTipo.LEMBRETE,
                    metadata);

                sendEmailIfAvailable(utente.getEmail(),
                    () -> emailService.sendAppointmentReminderOneDay(utente.getEmail(), marcacao.getData()));
            }
            }
        }
    }

    private void sendEmailIfAvailable(String email, Runnable sender) {
        if (email == null || email.isBlank()) {
            return;
        }
        try {
            sender.run();
        } catch (Exception e) {
            logger.error("Falha ao enviar email para {}", email, e);
        }
    }
}
