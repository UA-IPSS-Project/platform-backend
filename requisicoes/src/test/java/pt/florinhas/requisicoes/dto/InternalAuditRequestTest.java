package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InternalAuditRequestTest {

    @Test
    void constructorAndAccessors_DeveFuncionar() {

        InternalAuditRequest request =
                new InternalAuditRequest(
                        1L,
                        "Nuno",
                        "CREATE",
                        "REQUISICAO",
                        2L,
                        "Detalhes",
                        "127.0.0.1");

        assertEquals(
                1L,
                request.userId());

        assertEquals(
                "Nuno",
                request.userName());

        assertEquals(
                "CREATE",
                request.action());

        assertEquals(
                "REQUISICAO",
                request.entityType());

        assertEquals(
                2L,
                request.entityId());

        assertEquals(
                "Detalhes",
                request.details());

        assertEquals(
                "127.0.0.1",
                request.ipAddress());
    }
}