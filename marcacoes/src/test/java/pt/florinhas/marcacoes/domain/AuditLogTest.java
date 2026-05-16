package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class AuditLogTest {

    @Test
    void deveCriarAuditLogComBuilder() {

        LocalDateTime now = LocalDateTime.now();

        AuditLog log = AuditLog.builder()
                .id(1L)
                .userId(2L)
                .userName("Nuno")
                .action("LOGIN")
                .entityType("USER")
                .entityId(10L)
                .details("Teste")
                .ipAddress("127.0.0.1")
                .timestamp(now)
                .build();

        assertEquals(1L, log.getId());
        assertEquals(2L, log.getUserId());
        assertEquals("Nuno", log.getUserName());
        assertEquals("LOGIN", log.getAction());
        assertEquals("USER", log.getEntityType());
        assertEquals(10L, log.getEntityId());
        assertEquals("Teste", log.getDetails());
        assertEquals("127.0.0.1", log.getIpAddress());
        assertEquals(now, log.getTimestamp());
    }

    @Test
    void onCreateDeveDefinirTimestamp() {

        AuditLog log = new AuditLog();

        log.onCreate();

        assertNotNull(log.getTimestamp());
    }
}