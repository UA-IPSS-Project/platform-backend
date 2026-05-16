package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.service.AuditLogService;
import pt.florinhas.marcacoes.repository.AuditLogRepository;

class AuditLogControllerTest {

    @Test
    @DisplayName("AuditLogController deve ser criado")
    void deveCriarController() {
        AuditLogService service = mock(AuditLogService.class);
        AuditLogRepository repository = mock(AuditLogRepository.class);
        AuditLogController controller = new AuditLogController(service, repository, "secret");
        assertNotNull(controller);
    }

    @Test
    @DisplayName("Classe AuditLogController deve carregar")
    void classeDeveCarregar() {

        assertNotNull(AuditLogController.class);
    }
}