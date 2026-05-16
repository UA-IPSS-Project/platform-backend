package pt.florinhas.marcacoes.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.marcacoes.domain.AuditLog;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String action;
    private String entityType;
    private Long entityId;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;

    public static AuditLogDTO fromEntity(AuditLog log) {
        return AuditLogDTO.builder()
            .id(log.getId())
            .userId(log.getUserId())
            .userName(log.getUserName())
            .action(log.getAction())
            .entityType(log.getEntityType())
            .entityId(log.getEntityId())
            .details(log.getDetails())
            .ipAddress(log.getIpAddress())
            .timestamp(log.getTimestamp())
            .build();
    }
}