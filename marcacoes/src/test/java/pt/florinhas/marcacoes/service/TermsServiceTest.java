package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

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
    void setup() {

        systemConfigService =
                mock(SystemConfigService.class);

        utilizadorRepository =
                mock(UtilizadorRepository.class);

        eventPublisher =
                mock(ApplicationEventPublisher.class);

        emailService =
                mock(EmailService.class);

        auditLogService =
                mock(AuditLogService.class);

        service =
                new TermsService(
                        systemConfigService,
                        utilizadorRepository,
                        eventPublisher,
                        emailService,
                        auditLogService
                );
    }

    @Test
    void getCurrentVersion_DeveRetornarVersaoAtual() {

        when(systemConfigService.getConfigValueAsInt(
                "terms.current.version",
                1
        )).thenReturn(5);

        int resultado =
                service.getCurrentVersion();

        assertEquals(5, resultado);
    }

    @Test
    void needsAcceptance_DeveRetornarTrueQuandoSemVersao() {

        Utilizador user =
                new Utilizador();

        when(systemConfigService.getConfigValueAsInt(
                "terms.current.version",
                1
        )).thenReturn(3);

        boolean resultado =
                service.needsAcceptance(user);

        assertTrue(resultado);
    }

    @Test
    void needsAcceptance_DeveRetornarTrueQuandoVersaoAntiga() {

        Utilizador user =
                new Utilizador();

        user.setTermsVersion(1);

        when(systemConfigService.getConfigValueAsInt(
                "terms.current.version",
                1
        )).thenReturn(3);

        boolean resultado =
                service.needsAcceptance(user);

        assertTrue(resultado);
    }

    @Test
    void needsAcceptance_DeveRetornarFalse() {

        Utilizador user =
                new Utilizador();

        user.setTermsVersion(3);

        when(systemConfigService.getConfigValueAsInt(
                "terms.current.version",
                1
        )).thenReturn(3);

        boolean resultado =
                service.needsAcceptance(user);

        assertFalse(resultado);
    }

    @Test
    void getStatus_DeveRetornarDTO() {

        Utilizador user =
                new Utilizador();

        user.setTermsVersion(2);

        when(systemConfigService.getConfigValueAsInt(
                "terms.current.version",
                1
        )).thenReturn(5);

        TermsStatusDTO dto =
                service.getStatus(user);

        assertEquals(
                5,
                dto.getCurrentVersion()
        );

        assertEquals(
                2,
                dto.getUserVersion()
        );

        assertTrue(
                dto.isNeedsAcceptance()
        );
    }

    @Test
    void acceptTerms_DeveGuardarVersaoAtual() {

        Utilizador user =
                new Utilizador();

        when(systemConfigService.getConfigValueAsInt(
                "terms.current.version",
                1
        )).thenReturn(4);

        service.acceptTerms(user);

        assertEquals(
                4,
                user.getTermsVersion()
        );

        verify(utilizadorRepository)
                .save(user);
    }

    @Test
    void acceptTermsComVersao_DeveGuardarQuandoVersaoAtual() {

        Utilizador user =
                new Utilizador();

        user.setId(1L);

        when(systemConfigService.getConfigValueAsInt(
                "terms.current.version",
                1
        )).thenReturn(3);

        service.acceptTerms(user, 3);

        assertEquals(
                3,
                user.getTermsVersion()
        );

        verify(utilizadorRepository)
                .save(user);

        verify(auditLogService)
                .log(
                        eq("ACEITAR_TERMOS"),
                        eq("UTILIZADOR"),
                        eq(1L),
                        contains("v3")
                );
    }

    @Test
    void acceptTermsComVersao_NaoDeveGuardarQuandoVersaoErrada() {

        Utilizador user =
                new Utilizador();

        when(systemConfigService.getConfigValueAsInt(
                "terms.current.version",
                1
        )).thenReturn(5);

        service.acceptTerms(user, 2);

        verify(utilizadorRepository, never())
                .save(any());

        verify(auditLogService, never())
                .log(any(), any(), any(), any());
    }

    @Test
    void updateTermsVersion_DeveAtualizarEPublicarEvento() {

        service.updateTermsVersion(
                10,
                "Mudanças importantes"
        );

        verify(systemConfigService)
                .setConfigValue(
                        eq("terms.current.version"),
                        eq("10"),
                        anyString()
                );

        verify(auditLogService)
                .log(
                        eq("ATUALIZAR_VERSAO_TERMOS"),
                        eq("SYSTEM_CONFIG"),
                        isNull(),
                        contains("v10")
                );

        verify(eventPublisher)
                .publishEvent(any(TermsPublishedEvent.class));
    }

    @Test
    void updateTermsContent_DeveAtualizarPT() {

        service.updateTermsContent(
                "pt",
                "conteudo pt"
        );

        verify(systemConfigService)
                .setConfigValue(
                        eq("terms.content.pt"),
                        eq("conteudo pt"),
                        anyString()
                );
    }

    @Test
    void updateTermsContent_DeveAtualizarEN() {

        service.updateTermsContent(
                "en",
                "english"
        );

        verify(systemConfigService)
                .setConfigValue(
                        eq("terms.content.en"),
                        eq("english"),
                        anyString()
                );
    }

    @Test
    void getTermsContent_DeveRetornarPT() {

        when(systemConfigService.getConfigValue(
                "terms.content.pt",
                ""
        )).thenReturn("conteudo");

        String resultado =
                service.getTermsContent("pt");

        assertEquals(
                "conteudo",
                resultado
        );
    }

    @Test
    void getTermsContent_DeveRetornarEN() {

        when(systemConfigService.getConfigValue(
                "terms.content.en",
                ""
        )).thenReturn("english");

        String resultado =
                service.getTermsContent("en");

        assertEquals(
                "english",
                resultado
        );
    }

    @Test
    void publishTerms_DevePublicarNovaVersao() {

        when(systemConfigService.getConfigValueAsInt(
                "terms.current.version",
                1
        )).thenReturn(5);

        int resultado =
                service.publishTerms(
                        "pt",
                        "en",
                        "mudancas",
                        99L
                );

        assertEquals(6, resultado);

        verify(systemConfigService, times(3))
                .setConfigValue(
                        anyString(),
                        anyString(),
                        anyString()
                );

        verify(auditLogService)
                .log(
                        eq("PUBLICAR_TERMOS"),
                        eq("SYSTEM_CONFIG"),
                        isNull(),
                        contains("v6")
                );

        verify(eventPublisher)
                .publishEvent(any(TermsPublishedEvent.class));
    }

    @Test
    void notifyOutdatedUsers_DeveEnviarEmails() {

        Utilizador user =
                new Utilizador();

        user.setId(1L);
        user.setNome("Joao");
        user.setEmail("joao@test.com");

        TermsPublishedEvent event =
                new TermsPublishedEvent(
                        10,
                        "Mudancas"
                );

        when(utilizadorRepository
                .findOutdatedTermsUsers(10))
                .thenReturn(List.of(user));

        service.notifyOutdatedUsers(event);

        verify(emailService)
                .sendGenericEmail(
                        eq("joao@test.com"),
                        argThat(subject ->
                                subject.contains("Termos de Uso")
                        ),
                        argThat(body ->
                                body.contains("versão 10")
                        )
                );
    }

    @Test
    void notifyOutdatedUsers_DeveContinuarQuandoEmailFalha() {

        Utilizador user =
                new Utilizador();

        user.setId(1L);
        user.setNome("Joao");
        user.setEmail("joao@test.com");

        TermsPublishedEvent event =
                new TermsPublishedEvent(
                        10,
                        "Mudancas"
                );

        when(utilizadorRepository
                .findOutdatedTermsUsers(10))
                .thenReturn(List.of(user));

        doThrow(new RuntimeException("Erro email"))
                .when(emailService)
                .sendGenericEmail(
                        anyString(),
                        anyString(),
                        anyString()
                );

        assertDoesNotThrow(() ->
                service.notifyOutdatedUsers(event)
        );

        verify(emailService)
                .sendGenericEmail(
                        eq("joao@test.com"),
                        argThat(subject ->
                                subject.contains("Termos de Uso")
                        ),
                        argThat(body ->
                                body.contains("versão 10")
                        )
                );
    }

    @Test
    void notifyOutdatedUsers_DeveIgnorarListaVazia() {

        TermsPublishedEvent event =
                new TermsPublishedEvent(
                        10,
                        "Mudancas"
                );

        when(utilizadorRepository
                .findOutdatedTermsUsers(10))
                .thenReturn(List.of());

        service.notifyOutdatedUsers(event);

        verify(emailService, never())
                .sendGenericEmail(
                        anyString(),
                        anyString(),
                        anyString()
                );
    }
}