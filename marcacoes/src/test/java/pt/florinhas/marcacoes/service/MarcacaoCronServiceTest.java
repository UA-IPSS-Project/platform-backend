package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.marcacoes.domain.AtendimentoTipo;
import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoSecretaria;
import pt.florinhas.marcacoes.repository.MarcacaoRepository;
import pt.florinhas.marcacoes.service.email.EmailService;

class MarcacaoCronServiceTest {

    private MarcacaoRepository marcacaoRepository;
    private NotificacaoService notificacaoService;
    private EmailService emailService;
    private MarcacaoCronService service;

    @BeforeEach
    void setUp() {
        marcacaoRepository = mock(MarcacaoRepository.class);
        notificacaoService = mock(NotificacaoService.class);
        emailService = mock(EmailService.class);
        service = new MarcacaoCronService(marcacaoRepository, notificacaoService, emailService);
    }

    @Test
    void notificarMarcacoesEmUmDia_DeveNotificarUtenteComEmail() {

        Utente utente = new Utente();
        utente.setId(1L);
        utente.setEmail("utente@test.com");

        MarcacaoSecretaria secretaria = new MarcacaoSecretaria();
        secretaria.setUtente(utente);
        secretaria.setAssunto("Consulta");
        secretaria.setTipoAtendimento(AtendimentoTipo.PRESENCIAL);

        Marcacao marcacao = new Marcacao();
        marcacao.setId(10L);
        marcacao.setData(LocalDateTime.now().plusDays(1));
        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setMarcacaoSecretaria(secretaria);

        when(marcacaoRepository.findMarcacoesBetweenDates(any(), any(), any()))
                .thenReturn(List.of(marcacao));

        service.notificarMarcacoesEmUmDia();

        verify(notificacaoService).notificarLembreteUmDia(1L, 10L, marcacao.getData());
        verify(emailService).sendAppointmentReminderOneDay("utente@test.com", marcacao.getData(), "Consulta");
    }

    @Test
    void marcarAgendadasExpiradas_DeveMarcarComoInvalido() {

        Marcacao marcacao = new Marcacao();
        marcacao.setEstado(EventoEstado.AGENDADO);

        when(marcacaoRepository.findByEstadoAndDataBefore(any(), any()))
                .thenReturn(List.of(marcacao));

        service.marcarAgendadasExpiradas();

        assertEquals(EventoEstado.INVALIDO, marcacao.getEstado());
        verify(marcacaoRepository).saveAll(List.of(marcacao));
    }

    @Test
    void concluirEmProgressoExpiradas_DeveMarcarComoConcluido() {

        Marcacao marcacao = new Marcacao();
        marcacao.setEstado(EventoEstado.EM_PROGRESSO);

        when(marcacaoRepository.findByEstadoAndDataBefore(any(), any()))
                .thenReturn(List.of(marcacao));

        service.concluirEmProgressoExpiradas();

        assertEquals(EventoEstado.CONCLUIDO, marcacao.getEstado());
        verify(marcacaoRepository).saveAll(List.of(marcacao));
    }
}