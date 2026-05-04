package pt.florinhas.marcacoes.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.exception.BadRequestException;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.dto.TermsStatusDTO;
import pt.florinhas.marcacoes.service.email.EmailService;

/**
 * Serviço de versionamento dos Termos de Uso.
 *
 * A versão atual é gerida via system_config (chave "terms.current.version").
 * Quando a versão é incrementada, todos os utilizadores com versão inferior
 * são notificados por email e forçados a re-aceitar no próximo login.
 */
@Service
@Slf4j
public class TermsService {

    /** Chave na tabela system_config para a versão atual dos termos. */
    static final String CONFIG_KEY = "terms.current.version";
    static final int DEFAULT_VERSION = 1;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private UtilizadorRepository utilizadorRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditLogService auditLogService;

    // -------------------------------------------------------
    // Consulta de estado
    // -------------------------------------------------------

    public int getCurrentVersion() {
        return systemConfigService.getConfigValueAsInt(CONFIG_KEY, DEFAULT_VERSION);
    }

    public boolean needsAcceptance(Utilizador user) {
        return user.getTermsVersion() == null || user.getTermsVersion() < getCurrentVersion();
    }

    public TermsStatusDTO getStatus(Utilizador user) {
        int current = getCurrentVersion();
        return new TermsStatusDTO(current, user.getTermsVersion(), needsAcceptance(user));
    }

    // -------------------------------------------------------
    // Aceitação pelo utilizador
    // -------------------------------------------------------

    @Transactional
    public void acceptTerms(Utilizador user, int version) {
        int current = getCurrentVersion();
        if (version != current) {
            throw new BadRequestException("Versão dos termos inválida. Versão atual: " + current);
        }
        user.setTermsVersion(version);
        user.setTermsAcceptedAt(LocalDateTime.now());
        utilizadorRepository.save(user);

        auditLogService.log(
            "ACEITAR_TERMOS",
            "UTILIZADOR",
            user.getId(),
            "Termos aceites — versão " + version
        );
        log.info("Utilizador ID {} aceitou os termos versão {}", user.getId(), version);
    }

    // -------------------------------------------------------
    // Atualização da versão (admin/secretaria)
    // -------------------------------------------------------

    @Transactional
    public void updateTermsVersion(int newVersion, String changeDescription) {
        int current = getCurrentVersion();
        if (newVersion <= current) {
            throw new BadRequestException(
                "Nova versão (" + newVersion + ") deve ser superior à atual (" + current + ")");
        }

        systemConfigService.setConfigValue(
            CONFIG_KEY,
            String.valueOf(newVersion),
            "Versão atual dos Termos de Uso"
        );

        auditLogService.log(
            "ATUALIZAR_TERMOS",
            "SYSTEM_CONFIG",
            null,
            String.format("Versão dos Termos de Uso atualizada: %d → %d. Alterações: %s",
                current, newVersion,
                changeDescription != null ? changeDescription : "sem descrição")
        );

        log.info("Versão dos termos atualizada: {} → {}", current, newVersion);

        // Notificar todos os utilizadores com versão desatualizada
        notifyOutdatedUsers(newVersion, changeDescription);
    }

    private void notifyOutdatedUsers(int newVersion, String changeDescription) {
        List<Utilizador> outdated = utilizadorRepository.findAll().stream()
            .filter(u -> u.getEmail() != null && !u.getEmail().contains("@anonimizado.local"))
            .filter(u -> u.getTermsVersion() == null || u.getTermsVersion() < newVersion)
            .toList();

        log.info("Notificando {} utilizadores sobre atualização dos termos v{}", outdated.size(), newVersion);

        String subject = "Atualização dos Termos de Uso — Florinhas do Vouga";
        for (Utilizador u : outdated) {
            try {
                String body = buildNotificationEmail(u.getNome(), newVersion, changeDescription);
                emailService.sendGenericEmail(u.getEmail(), subject, body);
            } catch (Exception e) {
                log.error("Erro ao notificar utilizador ID {} sobre termos v{}: {}", u.getId(), newVersion, e.getMessage());
            }
        }
    }

    private String buildNotificationEmail(String nome, int version, String changeDescription) {
        return String.format(
            "Olá %s,%n%n" +
            "Os Termos de Uso da plataforma Florinhas do Vouga foram atualizados para a versão %d.%n%n" +
            "%s%n%n" +
            "Na próxima vez que aceder à plataforma, ser-lhe-á pedido que reveja e aceite os novos termos.%n%n" +
            "Atenciosamente,%n" +
            "Equipa Florinhas do Vouga",
            nome, version,
            changeDescription != null && !changeDescription.isBlank()
                ? "Principais alterações:\n" + changeDescription
                : "Por favor, reveja os termos atualizados."
        );
    }

    // -------------------------------------------------------
    // Gestão de Conteúdo (PT/EN)
    // -------------------------------------------------------

    public String getTermsContent(String lang) {
        String key = "system.terms.content." + (lang.equalsIgnoreCase("en") ? "en" : "pt");
        return systemConfigService.getConfigValue(key, "");
    }

    @Transactional
    public void updateTermsContent(String lang, String content) {
        String key = "system.terms.content." + (lang.equalsIgnoreCase("en") ? "en" : "pt");
        String desc = "Conteúdo dos Termos de Uso (" + lang.toUpperCase() + ")";
        systemConfigService.setConfigValue(key, content, desc);

        auditLogService.log(
            "ATUALIZAR_CONTEUDO_TERMOS",
            "SYSTEM_CONFIG",
            null,
            "Conteúdo dos Termos de Uso atualizado para idioma: " + lang.toUpperCase()
        );
    }

    /**
     * Publica nova versão dos termos: guarda conteúdo PT+EN e incrementa versão atomicamente.
     * Notifica todos os utilizadores por email.
     */
    @Transactional
    public int publishTerms(String contentPt, String contentEn, String changeDescription) {
        int current = getCurrentVersion();
        int newVersion = current + 1;

        // Guardar conteúdo
        systemConfigService.setConfigValue("system.terms.content.pt", contentPt, "Conteúdo dos Termos de Uso (PT)");
        systemConfigService.setConfigValue("system.terms.content.en", contentEn, "Conteúdo dos Termos de Uso (EN)");

        // Incrementar versão
        systemConfigService.setConfigValue(CONFIG_KEY, String.valueOf(newVersion), "Versão atual dos Termos de Uso");

        auditLogService.log(
            "PUBLICAR_TERMOS",
            "SYSTEM_CONFIG",
            null,
            String.format("Termos publicados: v%d → v%d. Alterações: %s",
                current, newVersion,
                changeDescription != null ? changeDescription : "sem descrição")
        );

        log.info("Termos publicados: v{} → v{}", current, newVersion);
        notifyOutdatedUsers(newVersion, changeDescription);
        return newVersion;
    }
}
