package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AuditLogTest {

    @Test
    void onCreate_DeveDefinirTimestamp() {

        AuditLog log = new AuditLog();

        log.onCreate();

        assertNotNull(log.getTimestamp());
    }
}