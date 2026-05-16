package pt.florinhas.marcacoes.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import pt.florinhas.marcacoes.domain.AuditLog;
import pt.florinhas.marcacoes.dto.AuditLogDTO;
import pt.florinhas.marcacoes.dto.InternalAuditRequest;
import pt.florinhas.marcacoes.repository.AuditLogRepository;
import pt.florinhas.marcacoes.service.AuditLogService;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final String gatewaySharedSecret;
    private final AuditLogService auditLogService;
    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogService auditLogService,
            AuditLogRepository auditLogRepository,
            @Value("${gateway.shared-secret:florinhas}") String gatewaySharedSecret) {
        this.auditLogService = auditLogService;
        this.auditLogRepository = auditLogRepository;
        this.gatewaySharedSecret = gatewaySharedSecret;
    }

    private static final int MAX_PAGE_SIZE = 200;

    @GetMapping("/logs")
    @PreAuthorize("hasRole('DPO')")
    public ResponseEntity<?> getLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        if (page < 0)
            return ResponseEntity.badRequest().body("O parâmetro 'page' deve ser >= 0.");
        if (size < 1 || size > MAX_PAGE_SIZE)
            return ResponseEntity.badRequest().body("O parâmetro 'size' deve estar entre 1 e " + MAX_PAGE_SIZE + ".");

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogDTO> logs = auditLogService
                .findWithFilters(userId, action, entityType, startDate, endDate, pageable)
                .map(AuditLogDTO::fromEntity);
        return ResponseEntity.ok(logs);
    }

    /**
     * Endpoint interno: permite ao api-gateway registar eventos de autenticação
     * (login, registo, logout) sem passar pelo contexto de segurança normal.
     * Autenticado pelo cabeçalho X-Gateway-Secret.
     */
    @PostMapping("/internal/log")
    public ResponseEntity<Void> logInternal(
            @RequestHeader("X-Gateway-Secret") String secret,
            @RequestBody InternalAuditRequest req) {

        if (!gatewaySharedSecret.equals(secret)) {
            return ResponseEntity.status(403).build();
        }

        AuditLog entry = AuditLog.builder()
                .userId(req.userId())
                .userName(req.userName())
                .action(req.action())
                .entityType(req.entityType())
                .entityId(req.entityId())
                .details(req.details())
                .ipAddress(req.ipAddress())
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(entry);
        return ResponseEntity.ok().build();
    }
}