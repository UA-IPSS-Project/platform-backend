package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import pt.florinhas.marcacoes.dto.AuditLogDTO;
import pt.florinhas.marcacoes.dto.InternalAuditRequest;
import pt.florinhas.marcacoes.repository.AuditLogRepository;
import pt.florinhas.marcacoes.service.AuditLogService;

class AuditLogControllerTest {

    private AuditLogService service;
    private AuditLogRepository repository;

    private AuditLogController controller;

    @BeforeEach
    void setUp() throws Exception {

        service =
                mock(AuditLogService.class);

        repository =
                mock(AuditLogRepository.class);

        controller =
                new AuditLogController();

        setField("auditLogService", service);
        setField("auditLogRepository", repository);
        setField("gatewaySharedSecret", "secret");
    }

    @Test
    void getLogs_DeveRetornarBadRequest() {

        ResponseEntity<?> result =
                controller.getLogs(
                        null,
                        null,
                        null,
                        null,
                        null,
                        -1,
                        50);

        assertEquals(400, result.getStatusCode().value());
    }

    @Test
    void getLogs_DeveRetornarPagina() {

        when(service.findWithFilters(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(
                        java.util.List.of(),
                        PageRequest.of(0, 10),
                        0));

        ResponseEntity<?> result =
                controller.getLogs(
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        10);

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void logInternal_DeveRetornar403() {

        ResponseEntity<Void> result =
                controller.logInternal(
                        "wrong",
                        new InternalAuditRequest(
                                1L,
                                "Nome",
                                "LOGIN",
                                "USER",
                                1L,
                                "Teste",
                                "127.0.0.1"));

        assertEquals(403, result.getStatusCode().value());
    }

    private void setField(
            String fieldName,
            Object value
    ) throws Exception {

        Field field =
                AuditLogController.class
                        .getDeclaredField(fieldName);

        field.setAccessible(true);

        field.set(controller, value);
    }
}