package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.dto.TermsStatusDTO;
import pt.florinhas.marcacoes.event.TermsPublishedEvent;
import pt.florinhas.marcacoes.service.email.EmailService;

class TermsServiceTest {

    private SystemConfigService systemConfigService;
    private UtilizadorRepository utilizadorRepository;
    private ApplicationEventPublisher eventPublisher;
    private EmailService emailService;
    private AuditLogService auditLogService;

    private TermsService service;

    @BeforeEach
    void setUp() {

        systemConfigService = mock(SystemConfigService.class);
        utilizadorRepository = mock(UtilizadorRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        emailService = mock(EmailService.class);
        auditLogService = mock(AuditLogService.class);

        service = new TermsService(
                systemConfigService,
                utilizadorRepository,
                eventPublisher,
                emailService,
                auditLogService);
    }

    @Test
    void getCurrentVersion_DeveRetornarVersao() {

        when(systemConfigService.getConfigValueAsInt(anyString(), anyInt()))
                .thenReturn(5);

        int result = service.getCurrentVersion();

        assertEquals(5, result);
    }

    @Test
    void needsAcceptance_DeveRetornarTrueQuandoNull() {

        Utilizador user = new Utente();

        when(systemConfigService.getConfigValueAsInt(anyString(), anyInt()))
                .thenReturn(3);

        assertTrue(service.needsAcceptance(user));
    }

    @Test
    void needsAcceptance_DeveRetornarTrueQuandoInferior() {

        Utilizador user = new Utente();

        user.setTermsVersion(1);

        when(systemConfigService.getConfigValueAsInt(anyString(), anyInt()))
                .thenReturn(3);

        assertTrue(service.needsAcceptance(user));
    }

    @Test
    void needsAcceptance_DeveRetornarFalse() {

        Utilizador user = new Utente();

        user.setTermsVersion(3);

        when(systemConfigService.getConfigValueAsInt(anyString(), anyInt()))
                .thenReturn(3);

        assertFalse(service.needsAcceptance(user));
    }

    @Test
    void getStatus_DeveRetornarDto() {

        Utilizador user = new Utente();

        user.setTermsVersion(2);

        when(systemConfigService.getConfigValueAsInt(anyString(), anyInt()))
                .thenReturn(5);

        TermsStatusDTO result = service.getStatus(user);

        assertEquals(5, result.getCurrentVersion());
        assertEquals(2, result.getUserVersion());
        assertTrue(result.isNeedsAcceptance());
    }

    @Test
    void acceptTerms_DeveGuardar() {

        Utilizador user = new Utente();

        when(systemConfigService.getConfigValueAsInt(anyString(), anyInt()))
                .thenReturn(5);

        service.acceptTerms(user);

        assertEquals(5, user.getTermsVersion());

        verify(utilizadorRepository).save(user);
    }

    @Test
    void acceptTermsComVersao_DeveGuardar() {

        Utilizador user = new Utente();

        user.setId(1L);

        when(systemConfigService.getConfigValueAsInt(anyString(), anyInt()))
                .thenReturn(5);

        service.acceptTerms(user, 5);

        assertEquals(5, user.getTermsVersion());

        verify(utilizadorRepository).save(user);

        verify(auditLogService).log(
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void acceptTermsComVersao_NaoDeveGuardarQuandoInvalida() {

        Utilizador user = new Utente();

        when(systemConfigService.getConfigValueAsInt(anyString(), anyInt()))
                .thenReturn(5);

        service.acceptTerms(user, 4);

        verify(utilizadorRepository, never()).save(any());
    }

    @Test
    void updateTermsVersion_DeveAtualizar() {

        service.updateTermsVersion(6, "Mudanças");

        verify(systemConfigService).setConfigValue(
                any(),
                any(),
                any());

        verify(eventPublisher).publishEvent(any(TermsPublishedEvent.class));

        verify(auditLogService).log(
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void updateTermsContent_DeveAtualizarPt() {

        service.updateTermsContent("pt", "conteudo");

        verify(systemConfigService).setConfigValue(
                any(),
                any(),
                any());

        verify(auditLogService).log(
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void updateTermsContent_DeveAtualizarEn() {

        service.updateTermsContent("en", "content");

        verify(systemConfigService).setConfigValue(
                any(),
                any(),
                any());
    }

    @Test
    void getTermsContent_DeveRetornarPt() {

        when(systemConfigService.getConfigValue(anyString(), anyString()))
                .thenReturn("conteudo");

        String result = service.getTermsContent("pt");

        assertEquals("conteudo", result);
    }

    @Test
    void getTermsContent_DeveRetornarEn() {

        when(systemConfigService.getConfigValue(anyString(), anyString()))
                .thenReturn("content");

        String result = service.getTermsContent("en");

        assertEquals("content", result);
    }

    @Test
    void publishTerms_DevePublicar() {

        when(systemConfigService.getConfigValueAsInt(anyString(), anyInt()))
                .thenReturn(5);

        int result = service.publishTerms(
                "pt",
                "en",
                "mudanças",
                1L);

        assertEquals(6, result);

        verify(systemConfigService, org.mockito.Mockito.times(3))
        .setConfigValue(
                any(),
                any(),
                any());

        verify(eventPublisher).publishEvent(any(TermsPublishedEvent.class));

        verify(auditLogService).log(
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void notifyOutdatedUsers_DeveEnviarEmails() {

        Utente user = new Utente();

        user.setId(1L);
        user.setNome("Nuno");
        user.setEmail("teste@test.com");

        when(utilizadorRepository.findOutdatedTermsUsers(5))
                .thenReturn(List.of(user));

        TermsPublishedEvent event =
                new TermsPublishedEvent(5, "Mudanças");

        service.notifyOutdatedUsers(event);

        verify(emailService).sendGenericEmail(
                any(),
                any(),
                any());
    }

    @Test
    void notifyOutdatedUsers_DeveIgnorarErroEmail() {

        Utente user = new Utente();

        user.setId(1L);
        user.setNome("Nuno");
        user.setEmail("teste@test.com");

        when(utilizadorRepository.findOutdatedTermsUsers(5))
                .thenReturn(List.of(user));

        TermsPublishedEvent event =
                new TermsPublishedEvent(5, "Mudanças");

        doThrow(new RuntimeException())
            .when(emailService)
            .sendGenericEmail(
                    any(),
                    any(),
                    any());

        service.notifyOutdatedUsers(event);

        verify(emailService).sendGenericEmail(
                any(),
                any(),
                any());
    }

    @Test
    void notifyOutdatedUsers_DeveFuncionarSemUtilizadores() {

        when(utilizadorRepository.findOutdatedTermsUsers(5))
                .thenReturn(List.of());

        TermsPublishedEvent event =
                new TermsPublishedEvent(5, "Mudanças");

        service.notifyOutdatedUsers(event);

        verify(emailService, never()).sendGenericEmail(
                any(),
                any(),
                any());
    }
}