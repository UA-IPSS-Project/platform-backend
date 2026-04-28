package pt.florinhas.marcacoes.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.service.email.EmailService;
import pt.florinhas.common_data.domain.Utilizador;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
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
                    emailService.sendAppointmentReminderOneDay(utente.getEmail(), marcacao.getData());
                }
            }
        }
    }
}
