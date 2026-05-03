package pt.florinhas.api_gateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Cliente HTTP reativo que envia eventos de auditoria de autenticação
 * (login, registo, logout) ao serviço de marcações, contornando o
 * contexto de segurança normal (que não está ainda populado).
 */
@Component
@Slf4j
public class AuditClient {

    private final WebClient webClient;
    private final String gatewaySharedSecret;

    public AuditClient(
            WebClient.Builder webClientBuilder,
            @Value("${gateway.marcacoes.base-url:http://marcacoes-backend:8081}") String marcacoesBaseUrl,
            @Value("${gateway.shared-secret:florinhas}") String gatewaySharedSecret) {
        this.webClient = webClientBuilder
                .baseUrl(marcacoesBaseUrl)
                .build();
        this.gatewaySharedSecret = gatewaySharedSecret;
    }

    public void logAsync(Long userId, String userName, String action,
                         String entityType, Long entityId, String details, String ipAddress) {
        var body = Map.of(
                "userId", userId != null ? userId : 0L,
                "userName", userName != null ? userName : "Sistema",
                "action", action,
                "entityType", entityType != null ? entityType : "AUTH",
                "entityId", entityId != null ? entityId : 0L,
                "details", details != null ? details : "",
                "ipAddress", ipAddress != null ? ipAddress : "unknown"
        );

        webClient.post()
                .uri("/api/audit/internal/log")
                .header("X-Gateway-Secret", gatewaySharedSecret)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(ex -> {
                    log.warn("Auditoria de auth falhou (não crítico): {}", ex.getMessage());
                    return Mono.empty();
                })
                .subscribe();
    }
}
