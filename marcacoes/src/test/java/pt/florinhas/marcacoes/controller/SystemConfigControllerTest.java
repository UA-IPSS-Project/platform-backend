package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.service.SystemConfigService;

class SystemConfigControllerTest {

    @Test
    @DisplayName("SystemConfigController deve ser criado")
    void deveCriarController() {

        SystemConfigService service =
                mock(SystemConfigService.class);

        SystemConfigController controller =
                new SystemConfigController(service);

        assertNotNull(controller);
    }

    @Test
    @DisplayName("Classe SystemConfigController deve carregar")
    void classeDeveCarregar() {

        assertNotNull(SystemConfigController.class);
    }
}