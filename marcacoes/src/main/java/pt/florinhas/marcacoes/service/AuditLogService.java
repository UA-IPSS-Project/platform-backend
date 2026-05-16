package pt.florinhas.marcacoes.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UtilizadorRepository utilizadorRepository;

    @Autowired(required = false)
    private HttpServletRequest request;

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
        if (request == null) return "unknown";
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "unknown";
    }
}