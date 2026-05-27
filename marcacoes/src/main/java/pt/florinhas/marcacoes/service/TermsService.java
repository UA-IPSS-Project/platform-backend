package pt.florinhas.marcacoes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.event.TermsPublishedEvent;
import pt.florinhas.marcacoes.dto.TermsStatusDTO;
import pt.florinhas.marcacoes.service.email.EmailService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TermsService {

    private static final String KEY_TERMS_VERSION = "terms.current.version";
    private static final String KEY_TERMS_PT = "terms.content.pt";
    private static final String KEY_TERMS_EN = "terms.content.en";

    private final SystemConfigService systemConfigService;
    private final UtilizadorRepository utilizadorRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    public int getCurrentVersion() {
        return systemConfigService.getConfigValueAsInt(KEY_TERMS_VERSION, 1);
    }

    public boolean needsAcceptance(Utilizador user) {
        return user.getTermsVersion() == null || user.getTermsVersion() < getCurrentVersion();
    }

    public TermsStatusDTO getStatus(Utilizador user) {
        return new TermsStatusDTO(
                getCurrentVersion(),
                user.getTermsVersion() != null ? user.getTermsVersion() : 0,
                needsAcceptance(user)
        );
    }

    @Transactional
    public void acceptTerms(Utilizador user) {
        user.setTermsVersion(getCurrentVersion());
        utilizadorRepository.save(user);
    }

    @Transactional
    public void acceptTerms(Utilizador user, int version) {
        if (version == getCurrentVersion()) {
            user.setTermsVersion(version);
            utilizadorRepository.save(user);
            auditLogService.log("ACEITAR_TERMOS", "UTILIZADOR", user.getId(), 
                "Termos de uso v" + version + " aceites.");
        }
    }

    @Transactional
    public void updateTermsVersion(int newVersion, String changeDescription) {
        systemConfigService.setConfigValue(KEY_TERMS_VERSION, String.valueOf(newVersion), "Versão atual dos termos");
        auditLogService.log("ATUALIZAR_VERSAO_TERMOS", "SYSTEM_CONFIG", null, 
            "Versão dos termos atualizada para v" + newVersion + ": " + changeDescription);
        
        eventPublisher.publishEvent(new TermsPublishedEvent(newVersion, changeDescription));
    }

    @Transactional
    @CacheEvict(value = "terms-content", allEntries = true)
    public void updateTermsContent(String lang, String content) {
        String key = "en".equalsIgnoreCase(lang) ? KEY_TERMS_EN : KEY_TERMS_PT;
        systemConfigService.setConfigValue(key, content, "Conteúdo dos termos em " + lang.toUpperCase());
        auditLogService.log("ATUALIZAR_CONTEUDO_TERMOS", "SYSTEM_CONFIG", null, 
            "Conteúdo dos termos em " + lang.toUpperCase() + " atualizado.");
    }

    @Cacheable(value = "terms-content", key = "#lang == null ? null : #lang.toLowerCase()")
    public String getTermsContent(String lang) {
        String key = "en".equalsIgnoreCase(lang) ? KEY_TERMS_EN : KEY_TERMS_PT;
        return systemConfigService.getConfigValue(key, "");
    }

    /**
     * Publica uma nova versão dos termos de forma atómica:
     * 1. Incrementa a versão e guarda o conteúdo PT/EN na mesma transação.
     * 2. Publica TermsPublishedEvent — o email só é enviado AFTER_COMMIT,
     *    garantindo que não há emails enviados em caso de rollback.
     */
    @Transactional
    @CacheEvict(value = "terms-content", allEntries = true)
    public int publishTerms(String contentPt, String contentEn, String changeDescription, Long publishedBy) {
        int newVersion = getCurrentVersion() + 1;

        systemConfigService.setConfigValue(KEY_TERMS_VERSION, String.valueOf(newVersion), "Versão atual dos termos");
        systemConfigService.setConfigValue(KEY_TERMS_PT, contentPt, "Conteúdo dos termos em PT");
        systemConfigService.setConfigValue(KEY_TERMS_EN, contentEn, "Conteúdo dos termos em EN");

        auditLogService.log("PUBLICAR_TERMOS", "SYSTEM_CONFIG", null,
                String.format("Termos v%d publicados por utilizador %d: %s", newVersion, publishedBy, changeDescription));

        // Evento disparado após commit — email enviado fora da transação
        eventPublisher.publishEvent(new TermsPublishedEvent(newVersion, changeDescription));

        return newVersion;
    }

    /**
     * Listener executado AFTER_COMMIT: a transação já foi confirmada,
     * por isso um rollback não pode desfazer emails já enviados.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifyOutdatedUsers(TermsPublishedEvent event) {
        // Query na BD — só carrega utilizadores desatualizados, não toda a tabela
        List<Utilizador> outdated = utilizadorRepository.findOutdatedTermsUsers(event.newVersion());

        log.info("Notificando {} utilizadores sobre atualização dos termos v{}", outdated.size(), event.newVersion());

        for (Utilizador user : outdated) {
            try {
                emailService.sendGenericEmail(
                        user.getEmail(),
                        "Atualização dos Termos de Uso — Florinhas do Vouga",
                        String.format(
                                "Olá %s,%n%nOs nossos Termos de Uso foram atualizados (versão %d).%n%s%n%n" +
                                "Na próxima vez que aceder à plataforma ser-lhe-á pedido que aceite os novos termos.",
                                user.getNome(), event.newVersion(), event.changeDescription()));
            } catch (Exception e) {
                log.error("Erro ao notificar utilizador ID {} sobre termos v{}: {}",
                        user.getId(), event.newVersion(), e.getMessage());
            }
        }
    }
}
