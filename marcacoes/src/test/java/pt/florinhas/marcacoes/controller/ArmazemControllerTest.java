package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.service.ArmazemService;
import pt.florinhas.marcacoes.service.AuditLogService;

class ArmazemControllerTest {

    @Test
    @DisplayName("ArmazemController deve ser criado")
    void deveCriarController() {

        ArmazemService armazemService =
                mock(ArmazemService.class);

        AuditLogService auditLogService =
                mock(AuditLogService.class);

        ArmazemController controller =
                new ArmazemController(
                        armazemService,
                        auditLogService
                );

        assertNotNull(controller);
    }

    @Test
    @DisplayName("Classe ArmazemController deve carregar")
    void classeDeveCarregar() {

        assertNotNull(ArmazemController.class);
    }
}