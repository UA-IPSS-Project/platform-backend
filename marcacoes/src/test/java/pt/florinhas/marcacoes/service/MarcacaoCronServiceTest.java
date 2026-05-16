package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.domain.Utente;
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
    void setup() {

        marcacaoRepository =
                mock(MarcacaoRepository.class);

        notificacaoService =
                mock(NotificacaoService.class);

        emailService =
                mock(EmailService.class);

        service =
                new MarcacaoCronService(
                        marcacaoRepository,
                        notificacaoService,
                        emailService
                );
    }

    @Test
    void notificarMarcacoesEmUmDia_DeveIgnorarListaVazia() {

        when(marcacaoRepository.findMarcacoesBetweenDates(
                any(),
                any(),
                eq("SECRETARIA")
        )).thenReturn(List.of());

        assertDoesNotThrow(() ->
                service.notificarMarcacoesEmUmDia()
        );

        verify(notificacaoService, never())
                .notificarLembreteUmDia(
                        anyLong(),
                        anyLong(),
                        any()
                );

        verify(emailService, never())
                .sendAppointmentReminderOneDay(
                        anyString(),
                        any()
                );
    }

    @Test
    void notificarMarcacoesEmUmDia_DeveEnviarNotificacaoEEmail() {

        Utente utente =
                new Utente();

        utente.setId(1L);
        utente.setEmail("test@test.com");

        MarcacaoSecretaria secretaria =
                new MarcacaoSecretaria();

        secretaria.setUtente(utente);

        Marcacao marcacao =
                new Marcacao();

        marcacao.setId(10L);
        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setData(LocalDateTime.now().plusDays(1));
        marcacao.setMarcacaoSecretaria(secretaria);

        when(marcacaoRepository.findMarcacoesBetweenDates(
                any(),
                any(),
                eq("SECRETARIA")
        )).thenReturn(List.of(marcacao));

        assertDoesNotThrow(() ->
                service.notificarMarcacoesEmUmDia()
        );

        verify(notificacaoService, times(1))
                .notificarLembreteUmDia(
                        eq(1L),
                        eq(10L),
                        any(LocalDateTime.class)
                );

        verify(emailService, times(1))
                .sendAppointmentReminderOneDay(
                        eq("test@test.com"),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void notificarMarcacoesEmUmDia_DeveEnviarSoNotificacaoSemEmail() {

        Utente utente =
                new Utente();

        utente.setId(1L);
        utente.setEmail("");

        MarcacaoSecretaria secretaria =
                new MarcacaoSecretaria();

        secretaria.setUtente(utente);

        Marcacao marcacao =
                new Marcacao();

        marcacao.setId(10L);
        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setData(LocalDateTime.now().plusDays(1));
        marcacao.setMarcacaoSecretaria(secretaria);

        when(marcacaoRepository.findMarcacoesBetweenDates(
                any(),
                any(),
                eq("SECRETARIA")
        )).thenReturn(List.of(marcacao));

        service.notificarMarcacoesEmUmDia();

        verify(notificacaoService, times(1))
                .notificarLembreteUmDia(
                        eq(1L),
                        eq(10L),
                        any(LocalDateTime.class)
                );

        verify(emailService, never())
                .sendAppointmentReminderOneDay(
                        anyString(),
                        any()
                );
    }

    @Test
    void notificarMarcacoesEmUmDia_DeveIgnorarMarcacaoNaoAgendada() {

        Marcacao marcacao =
                new Marcacao();

        marcacao.setEstado(EventoEstado.CANCELADO);

        when(marcacaoRepository.findMarcacoesBetweenDates(
                any(),
                any(),
                eq("SECRETARIA")
        )).thenReturn(List.of(marcacao));

        service.notificarMarcacoesEmUmDia();

        verify(notificacaoService, never())
                .notificarLembreteUmDia(
                        anyLong(),
                        anyLong(),
                        any()
                );

        verify(emailService, never())
                .sendAppointmentReminderOneDay(
                        anyString(),
                        any()
                );
    }

    @Test
    void notificarMarcacoesEmUmDia_DeveIgnorarSemSecretaria() {

        Marcacao marcacao =
                new Marcacao();

        marcacao.setEstado(EventoEstado.AGENDADO);

        when(marcacaoRepository.findMarcacoesBetweenDates(
                any(),
                any(),
                eq("SECRETARIA")
        )).thenReturn(List.of(marcacao));

        service.notificarMarcacoesEmUmDia();

        verify(notificacaoService, never())
                .notificarLembreteUmDia(
                        anyLong(),
                        anyLong(),
                        any()
                );

        verify(emailService, never())
                .sendAppointmentReminderOneDay(
                        anyString(),
                        any()
                );
    }

    @Test
    void notificarMarcacoesEmUmDia_DeveIgnorarSemUtente() {

        MarcacaoSecretaria secretaria =
                new MarcacaoSecretaria();

        Marcacao marcacao =
                new Marcacao();

        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setMarcacaoSecretaria(secretaria);

        when(marcacaoRepository.findMarcacoesBetweenDates(
                any(),
                any(),
                eq("SECRETARIA")
        )).thenReturn(List.of(marcacao));

        service.notificarMarcacoesEmUmDia();

        verify(notificacaoService, never())
                .notificarLembreteUmDia(
                        anyLong(),
                        anyLong(),
                        any()
                );

        verify(emailService, never())
                .sendAppointmentReminderOneDay(
                        anyString(),
                        any()
                );
    }
}