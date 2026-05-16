package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.service.AuthorizationService;
import pt.florinhas.marcacoes.service.TermsService;
import pt.florinhas.marcacoes.service.UtilizadorService;

class UtilizadorControllerTest {

    @Test
    @DisplayName("UtilizadorController deve ser criado")
    void deveCriarController() {
        UtilizadorService uService = mock(UtilizadorService.class);
        AuthorizationService aService = mock(AuthorizationService.class);
        TermsService tService = mock(TermsService.class);
        UtilizadorController controller = new UtilizadorController(uService, aService, tService);
        assertNotNull(controller);
    }

    @Test
    @DisplayName("Classe UtilizadorController deve carregar")
    void classeDeveCarregar() {

        assertNotNull(UtilizadorController.class);
    }
}