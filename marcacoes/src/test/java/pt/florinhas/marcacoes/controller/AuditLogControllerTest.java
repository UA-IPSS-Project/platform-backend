package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuditLogControllerTest {

    @Test
    @DisplayName("AuditLogController deve ser criado")
    void deveCriarController() {
        pt.florinhas.marcacoes.service.AuditLogService service = org.mockito.Mockito.mock(pt.florinhas.marcacoes.service.AuditLogService.class);
        pt.florinhas.marcacoes.repository.AuditLogRepository repository = org.mockito.Mockito.mock(pt.florinhas.marcacoes.repository.AuditLogRepository.class);
        AuditLogController controller = new AuditLogController(service, repository, "secret");
        assertNotNull(controller);
    }

    @Test
    @DisplayName("Classe AuditLogController deve carregar")
    void classeDeveCarregar() {

        assertNotNull(AuditLogController.class);
    }
}