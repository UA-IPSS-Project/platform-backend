package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UtilizadorControllerTest {

    @Test
    @DisplayName("UtilizadorController deve ser criado")
    void deveCriarController() {
        pt.florinhas.marcacoes.service.UtilizadorService uService = org.mockito.Mockito.mock(pt.florinhas.marcacoes.service.UtilizadorService.class);
        pt.florinhas.marcacoes.service.AuthorizationService aService = org.mockito.Mockito.mock(pt.florinhas.marcacoes.service.AuthorizationService.class);
        pt.florinhas.marcacoes.service.TermsService tService = org.mockito.Mockito.mock(pt.florinhas.marcacoes.service.TermsService.class);
        UtilizadorController controller = new UtilizadorController(uService, aService, tService);
        assertNotNull(controller);
    }

    @Test
    @DisplayName("Classe UtilizadorController deve carregar")
    void classeDeveCarregar() {

        assertNotNull(UtilizadorController.class);
    }
}