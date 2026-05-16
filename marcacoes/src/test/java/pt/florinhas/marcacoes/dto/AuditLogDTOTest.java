package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.domain.AuditLog;

class AuditLogDTOTest {

    @Test
    void fromEntity_DeveConverter() {

        LocalDateTime now = LocalDateTime.now();

        AuditLog log = AuditLog.builder()
                .id(1L)
                .userId(2L)
                .userName("Admin")
                .action("LOGIN")
                .entityType("USER")
                .entityId(10L)
                .details("Teste")
                .ipAddress("127.0.0.1")
                .timestamp(now)
                .build();

        AuditLogDTO dto =
                AuditLogDTO.fromEntity(log);

        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getUserId());
        assertEquals("Admin", dto.getUserName());
        assertEquals("LOGIN", dto.getAction());
        assertEquals("USER", dto.getEntityType());
        assertEquals(10L, dto.getEntityId());
        assertEquals("Teste", dto.getDetails());
        assertEquals("127.0.0.1", dto.getIpAddress());
        assertEquals(now, dto.getTimestamp());
    }
}