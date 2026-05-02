package pt.florinhas.marcacoes.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import pt.florinhas.marcacoes.dto.AuditLogDTO;
import pt.florinhas.marcacoes.service.AuditLogService;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('SECRETARIA')")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/logs")
    public ResponseEntity<Page<AuditLogDTO>> getLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogDTO> logs = auditLogService.findWithFilters(userId, action, entityType, startDate, endDate, pageable)
            .map(AuditLogDTO::fromEntity);
        return ResponseEntity.ok(logs);
    }
}
