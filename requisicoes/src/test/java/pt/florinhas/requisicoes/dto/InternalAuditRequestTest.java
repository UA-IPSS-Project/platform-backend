package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class InternalAuditRequestTest {

    @Test
    void record_DeveGuardarValores() {

        InternalAuditRequest request =
                new InternalAuditRequest(
                        1L,
                        "Ana",
                        "CRIAR",
                        "REQUISICAO",
                        10L,
                        "Detalhes",
                        "127.0.0.1");

        assertEquals(1L, request.userId());
        assertEquals("Ana", request.userName());
        assertEquals("CRIAR", request.action());
        assertEquals("REQUISICAO", request.entityType());
        assertEquals(10L, request.entityId());
        assertEquals("Detalhes", request.details());
        assertEquals("127.0.0.1", request.ipAddress());
    }
}