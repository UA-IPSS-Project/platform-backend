package pt.florinhas.marcacoes.dto;

public record InternalAuditRequest(
    Long userId,
    String userName,
    String action,
    String entityType,
    Long entityId,
    String details,
    String ipAddress
) {}
