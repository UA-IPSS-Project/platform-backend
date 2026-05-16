package pt.florinhas.marcacoes.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.domain.AuditLog;
import pt.florinhas.marcacoes.repository.AuditLogRepository;

@Service
@Slf4j
public class AuditLogService {

    private static final String IP_UNKNOWN = "unknown";

    private final AuditLogRepository auditLogRepository;
    private final UtilizadorRepository utilizadorRepository;
    private final ObjectProvider<HttpServletRequest> requestProvider;

    public AuditLogService(AuditLogRepository auditLogRepository,
            UtilizadorRepository utilizadorRepository,
            ObjectProvider<HttpServletRequest> requestProvider) {
        this.auditLogRepository = auditLogRepository;
        this.utilizadorRepository = utilizadorRepository;
        this.requestProvider = requestProvider;
    }

    @Transactional
    public void log(String action, String entityType, Long entityId, String details) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            Long userId = null;
            String userName = "Sistema";

            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                if (auth.getPrincipal() instanceof Utilizador user) {
                    userId = user.getId();
                    userName = user.getNome();
                } else {
                    String email = auth.getName();
                    try {
                        Utilizador user = utilizadorRepository.findByEmail(email).stream().findFirst().orElse(null);
                        if (user != null) {
                            userId = user.getId();
                            userName = user.getNome();
                        }
                    } catch (Exception e) {
                        log.warn("Erro ao obter utilizador para auditoria: {}", e.getMessage());
                    }
                }
            }

            String ipAddress = getClientIp();

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .userName(userName)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .ipAddress(ipAddress)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Auditoria registada: {} - {} - {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Erro ao registar auditoria: {}", e.getMessage(), e);
        }
    }

    public Page<AuditLog> findAll(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }

    public Page<AuditLog> findWithFilters(Long userId, String action, String entityType,
            LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        return auditLogRepository.findWithFilters(userId, action, entityType, startDate, endDate, pageable);
    }

    private String getClientIp() {
        HttpServletRequest request = requestProvider.getIfAvailable();
        if (request == null)
            return IP_UNKNOWN;

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || IP_UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || IP_UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : IP_UNKNOWN;
    }
}