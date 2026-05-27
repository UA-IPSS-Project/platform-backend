package pt.florinhas.marcacoes.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.service.email.EmailService;
import pt.florinhas.common_data.domain.Utilizador;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarcacaoCronService {

    private final MarcacaoRepository marcacaoRepository;
    private final NotificacaoService notificacaoService;
    private final EmailService emailService;

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

                notificacaoService.notificarLembreteUmDia(
                        utente.getId(),
                        marcacao.getId(),
                        marcacao.getData());

                if (utente.getEmail() != null && !utente.getEmail().isBlank()) {
                    String summary = marcacao.getMarcacaoSecretaria() != null
                            ? marcacao.getMarcacaoSecretaria().getAssunto() : null;
                    emailService.sendAppointmentReminderOneDay(utente.getEmail(), marcacao.getData(), summary);
                }
            }
        }
    }

    /**
     * Marca como INVALIDO marcações que continuam AGENDADO 2 dias após a data.
     * Corre todos os dias às 02:00.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void marcarAgendadasExpiradas() {
        LocalDateTime limite = LocalDateTime.now().minusDays(2);
        List<Marcacao> expiradas = marcacaoRepository.findByEstadoAndDataBefore(EventoEstado.AGENDADO, limite);
        for (Marcacao m : expiradas) {
            m.setEstado(EventoEstado.INVALIDO);
            log.info("[CRON] Marcação {} marcada como INVALIDO (estava AGENDADO, data={})", m.getId(), m.getData());
        }
        if (!expiradas.isEmpty()) {
            marcacaoRepository.saveAll(expiradas);
        }
    }

    /**
     * Marca como CONCLUIDO marcações que estão EM_PROGRESSO 2 dias após a data.
     * Corre todos os dias às 02:00.
     */
    @Scheduled(cron = "0 5 2 * * *")
    @Transactional
    public void concluirEmProgressoExpiradas() {
        LocalDateTime limite = LocalDateTime.now().minusDays(2);
        List<Marcacao> emProgresso = marcacaoRepository.findByEstadoAndDataBefore(EventoEstado.EM_PROGRESSO, limite);
        for (Marcacao m : emProgresso) {
            m.setEstado(EventoEstado.CONCLUIDO);
            log.info("[CRON] Marcação {} marcada como CONCLUIDO (estava EM_PROGRESSO, data={})", m.getId(), m.getData());
        }
        if (!emProgresso.isEmpty()) {
            marcacaoRepository.saveAll(emProgresso);
        }
    }
}
