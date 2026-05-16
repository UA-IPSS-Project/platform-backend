package pt.florinhas.requisicoes.dto;

public record InternalAuditRequest(
    Long userId,
    String userName,
    String action,
    String entityType,
    Long entityId,
    String details,
    String ipAddress
) {}