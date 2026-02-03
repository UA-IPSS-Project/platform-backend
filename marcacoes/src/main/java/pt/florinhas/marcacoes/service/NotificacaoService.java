package pt.florinhas.marcacoes.service;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pt.florinhas.marcacoes.domain.Notificacao;
import pt.florinhas.marcacoes.domain.NotificacaoTipo;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.dto.NotificacaoResponseDTO;
import pt.florinhas.marcacoes.exception.NotFoundException;
import pt.florinhas.marcacoes.repository.NotificacaoRepository;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UtilizadorRepository utilizadorRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate; // Inject Template

    @Transactional
    public Notificacao criarNotificacao(Long utilizadorId, String titulo, String mensagem, NotificacaoTipo tipo) {
        return criarNotificacao(utilizadorId, titulo, mensagem, tipo, null);
    }

    @Transactional
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
            org.slf4j.LoggerFactory.getLogger(NotificacaoService.class).info(
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
            org.slf4j.LoggerFactory.getLogger(NotificacaoService.class).info(
                    "WebSocket notification sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            // Log but don't fail transaction
            org.slf4j.LoggerFactory.getLogger(NotificacaoService.class).error("Failed to send websocket notification",
                    e);
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
    public void marcarComoLida(Long id) {
        Notificacao notificacao = notificacaoRepository.findById(id)
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
    public void eliminarNotificacao(Long id) {
        if (!notificacaoRepository.existsById(id)) {
            throw new NotFoundException("Notificação não encontrada");
        }
        notificacaoRepository.deleteById(id);
    }

    @Transactional
    public void eliminarTodas(Long utilizadorId) {
        notificacaoRepository.deleteByUtilizadorId(utilizadorId);
    }

    // --- Métodos de Negócio (Semantic Methods) ---

    @Transactional
    public void notificarNovaMarcacao(Utilizador utilizador, Long marcacaoId, LocalDateTime data, boolean isRemote) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("dd/MM/yyyy 'às' HH:mm");
        String dataFormatada = data.format(formatter);

        String tipoTexto = isRemote ? "remota " : "";
        String mensagem = "A sua marcação " + tipoTexto + "para " + dataFormatada + " foi agendada com sucesso.";
        String assunto = "Nova Marcação Agendada";

        Map<String, Object> metadata = Map.of("appointmentId", marcacaoId.toString());
        criarNotificacao(utilizador.getId(), assunto, mensagem, NotificacaoTipo.LEMBRETE, metadata);
        logSimulatedEmail(utilizador.getEmail(), assunto, mensagem);
    }

    @Transactional
    public void notificarCancelamento(Utilizador utilizador, LocalDateTime data) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("dd/MM/yyyy 'às' HH:mm");
        String dataFormatada = data.format(formatter);

        String mensagem = "A sua marcação de " + dataFormatada + " foi cancelada pelos serviços administrativos.";
        String assunto = "Marcação Cancelada";

        // Adicionar metadata com data e hora do slot cancelado
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        Map<String, Object> metadata = Map.of(
                "cancelledDate", data.format(dateFormatter),
                "cancelledTime", data.format(timeFormatter));

        criarNotificacao(utilizador.getId(), assunto, mensagem, NotificacaoTipo.CANCELAMENTO, metadata);
        logSimulatedEmail(utilizador.getEmail(), assunto, mensagem);
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
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        Map<String, Object> metadata = Map.of(
                "cancelledDate", data.format(dateFormatter),
                "cancelledTime", data.format(timeFormatter));

        criarNotificacao(destinatario.getId(), assunto, mensagem, NotificacaoTipo.CANCELAMENTO, metadata);
        // Admin notifications might not need email simulation, but keeping consistent
    }

    @Transactional
    public void notificarDocumentosInvalidos(Utilizador utilizador) {
        String mensagem = "Os documentos apresentados são inválidos. Por favor, contacte a secretaria.";
        String assunto = "Documentos Inválidos";

        criarNotificacao(utilizador.getId(), assunto, mensagem, NotificacaoTipo.LEMBRETE); // Using LEMBRETE as
                                                                                           // warning/info
        logSimulatedEmail(utilizador.getEmail(), assunto, mensagem);
    }

    private void logSimulatedEmail(String email, String assunto, String mensagem) {
        if (email != null) {
            org.slf4j.LoggerFactory.getLogger(NotificacaoService.class)
                    .info("Email simulado para {} com assunto: '{}' e mensagem: '{}'", email, assunto, mensagem);
        }
    }
}
