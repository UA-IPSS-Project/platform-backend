package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.domain.AuditLog;

class AuditLogDTOTest {

    @Test
    void fromEntity_DeveConverter() {

        AuditLog log = AuditLog.builder()
                .id(1L)
                .userId(2L)
                .userName("Nuno")
                .action("LOGIN")
                .entityType("USER")
                .entityId(3L)
                .details("Teste")
                .ipAddress("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        AuditLogDTO dto = AuditLogDTO.fromEntity(log);

        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getUserId());
        assertEquals("Nuno", dto.getUserName());
        assertEquals("LOGIN", dto.getAction());
    }
}