package pt.florinhas.notificacoes.service;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;
import pt.florinhas.common_data.domain.Notificacao;
import pt.florinhas.common_data.repository.NotificacaoRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.dto.NotificacaoResponseDTO;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy 'as' HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String METADATA_SUBTYPE_KEY = "notificationSubtype";

    private final NotificacaoRepository notificacaoRepository;
    private final UtilizadorRepository utilizadorRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public Notificacao criarNotificacao(Long utilizadorId, String titulo, String mensagem, String tipo) {
        return criarNotificacao(utilizadorId, titulo, mensagem, tipo, null);
    }

    public Notificacao criarNotificacao(Long utilizadorId, String titulo, String mensagem, String tipo,
            Map<String, Object> metadata) {
        Utilizador user = utilizadorRepository.findById(utilizadorId)
                .orElseThrow(() -> new IllegalArgumentException("Utilizador não encontrado"));
        return criarNotificacao(user, titulo, mensagem, tipo, metadata);
    }

    public Notificacao criarNotificacao(Utilizador user, String titulo, String mensagem, String tipo,
            Map<String, Object> metadata) {

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
                    user.getId(), user.getEmail(), titulo);
            String destination = (user.getEmail() != null && !user.getEmail().trim().isEmpty())
                    ? user.getEmail()
                    : user.getNif();

            messagingTemplate.convertAndSendToUser(
                    destination,
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
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada"));

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
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada"));
        notificacaoRepository.delete(notificacao);
    }

    @Transactional
    public void eliminarTodas(Long utilizadorId) {
        notificacaoRepository.deleteByUtilizadorId(utilizadorId);
    }

    // --- Métodos de Negócio (Side-effects, não devem falhar a transação principal)
    // ---

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notificarNovaMarcacao(Long utilizadorId, Long marcacaoId, LocalDateTime data, int durationMinutes,
            String summary) {
        String dataFormatada = data.format(DISPLAY_DATE_FORMATTER);
        String mensagem = "Marcacao criada para " + dataFormatada + ".";
        String assunto = "Marcacao Criada";

        Map<String, Object> metadata = Map.of(
                "appointmentId", marcacaoId.toString(),
                "createdDate", data.format(DATE_FORMATTER),
                "createdTime", data.format(TIME_FORMATTER),
                METADATA_SUBTYPE_KEY, "CREATED");

        criarNotificacao(utilizadorId, assunto, mensagem, "LEMBRETE", metadata);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notificarLembreteUmDia(Long utilizadorId, Long marcacaoId, LocalDateTime data) {
        String dataFormatada = data.format(DISPLAY_DATE_FORMATTER);
        String mensagem = "Relembramos que tem uma marcação amanhã, " + dataFormatada + ".";
        String assunto = "Lembrete de Marcação";

        Map<String, Object> metadata = Map.of(
                "appointmentId", marcacaoId.toString(),
                "appointmentDate", data.format(DATE_FORMATTER),
                "appointmentTime", data.format(TIME_FORMATTER),
                METADATA_SUBTYPE_KEY, "REMINDER_1_DAY");

        criarNotificacao(utilizadorId, assunto, mensagem, "LEMBRETE", metadata);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notificarCancelamento(Long utilizadorId, LocalDateTime data, String motivo) {
        String assunto = "Marcacao Cancelada";
        String motivoTexto = (motivo == null || motivo.isBlank()) ? "sem motivo especificado" : motivo;
        String mensagem = "Marcacao cancelada por " + motivoTexto + ".";

        Map<String, Object> metadata = Map.of(
                "cancelledDate", data.format(DATE_FORMATTER),
                "cancelledTime", data.format(TIME_FORMATTER),
                METADATA_SUBTYPE_KEY, "CANCELLED");

        criarNotificacao(utilizadorId, assunto, mensagem, "CANCELAMENTO", metadata);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notificarCancelamentoPeloUtente(Long destinatarioId, String nomeUtente,
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

        criarNotificacao(destinatarioId, assunto, mensagem, "CANCELAMENTO", metadata);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notificarDocumentosInvalidos(Long utilizadorId, String observacoes) {
        String mensagem = "Os documentos apresentados são inválidos. Por favor, contacte a secretaria. Observações: "
                + observacoes;
        String assunto = "Documentos Inválidos";

        criarNotificacao(utilizadorId, assunto, mensagem, "LEMBRETE", null);
    }

    // Cron job for 1 day reminders moved to marcacoes module
}
