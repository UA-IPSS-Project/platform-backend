package pt.florinhas.marcacoes.service;

import java.util.List;

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

    @Transactional
    public Notificacao criarNotificacao(Long utilizadorId, String titulo, String mensagem, NotificacaoTipo tipo) {
        Utilizador user = utilizadorRepository.findById(utilizadorId)
                .orElseThrow(() -> new NotFoundException("Utilizador não encontrado"));

        Notificacao notificacao = new Notificacao();
        notificacao.setUtilizador(user);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacao.setTipo(tipo);
        notificacao.setLida(false);

        return notificacaoRepository.save(notificacao);
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
    public void notificarNovaMarcacao(Utilizador utilizador, java.time.LocalDateTime data, boolean isRemote) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                .ofPattern("dd/MM/yyyy 'às' HH:mm");
        String dataFormatada = data.format(formatter);

        String tipoTexto = isRemote ? "remota " : "";
        String mensagem = "A sua marcação " + tipoTexto + "para " + dataFormatada + " foi agendada com sucesso.";
        String assunto = "Nova Marcação Criada";

        criarNotificacao(utilizador.getId(), assunto, mensagem, NotificacaoTipo.LEMBRETE);
        logSimulatedEmail(utilizador.getEmail(), assunto, mensagem);
    }

    @Transactional
    public void notificarCancelamento(Utilizador utilizador, java.time.LocalDateTime data) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                .ofPattern("dd/MM/yyyy 'às' HH:mm");
        String dataFormatada = data.format(formatter);

        String mensagem = "A sua marcação de " + dataFormatada + " foi cancelada pelos serviços administrativos.";
        String assunto = "Marcação Cancelada";

        criarNotificacao(utilizador.getId(), assunto, mensagem, NotificacaoTipo.CANCELAMENTO);
        logSimulatedEmail(utilizador.getEmail(), assunto, mensagem);
    }

    @Transactional
    public void notificarCancelamentoPeloUtente(Utilizador destinatario, String nomeUtente,
            java.time.LocalDateTime data) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                .ofPattern("dd/MM/yyyy 'às' HH:mm");
        String dataFormatada = data.format(formatter);

        String mensagem = "O utente " + nomeUtente + " cancelou a marcação de " + dataFormatada;
        String assunto = "Marcação Cancelada pelo Utente";

        criarNotificacao(destinatario.getId(), assunto, mensagem, NotificacaoTipo.CANCELAMENTO);
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
