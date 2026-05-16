package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class InternalAuditRequestTest {

    @Test
    void deveCriarRecord() {

        InternalAuditRequest request =
                new InternalAuditRequest(
                        1L,
                        "Admin",
                        "LOGIN",
                        "USER",
                        2L,
                        "Teste",
                        "127.0.0.1"
                );

        assertEquals(1L, request.userId());
        assertEquals("Admin", request.userName());
        assertEquals("LOGIN", request.action());
        assertEquals("USER", request.entityType());
        assertEquals(2L, request.entityId());
        assertEquals("Teste", request.details());
        assertEquals("127.0.0.1", request.ipAddress());
    }
}