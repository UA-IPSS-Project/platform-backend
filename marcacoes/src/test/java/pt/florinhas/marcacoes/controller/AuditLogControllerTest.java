package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuditLogControllerTest {

    @Test
    @DisplayName("AuditLogController deve ser criado")
    void deveCriarController() {

        AuditLogController controller =
                new AuditLogController();

        assertNotNull(controller);
    }

    @Test
    @DisplayName("Classe AuditLogController deve carregar")
    void classeDeveCarregar() {

        assertNotNull(AuditLogController.class);
    }
}