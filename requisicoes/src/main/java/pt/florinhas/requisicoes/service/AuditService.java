package pt.florinhas.requisicoes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.requisicoes.dto.InternalAuditRequest;

@Service
@Slf4j
public class AuditService {

    private final RestTemplate restTemplate;
    private final String marcacoesUrl;
    private final String gatewaySharedSecret;
    private final HttpServletRequest request;

    public AuditService(
            RestTemplate restTemplate,
            @Value("${marcacoes.url}") String marcacoesUrl,
            @Value("${gateway.shared-secret}") String gatewaySharedSecret,
            HttpServletRequest request) {
        this.restTemplate = restTemplate;
        this.marcacoesUrl = marcacoesUrl;
        this.gatewaySharedSecret = gatewaySharedSecret;
        this.request = request;
    }

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
                    userName = auth.getName();
                }
            }

            InternalAuditRequest auditReq = new InternalAuditRequest(
                userId,
                userName,
                action,
                entityType,
                entityId,
                details,
                getClientIp()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Gateway-Secret", gatewaySharedSecret);

            HttpEntity<InternalAuditRequest> entity = new HttpEntity<>(auditReq, headers);
            
            restTemplate.postForEntity(marcacoesUrl + "/api/audit/internal/log", entity, Void.class);
            log.debug("Auditoria enviada para marcacoes: {} - {} - {}", action, entityType, entityId);
        } catch (Exception e) {
            log.warn("Erro ao enviar auditoria (não crítico): {}", e.getMessage());
        }
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
