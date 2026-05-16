package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.service.TermsService;

class TermsControllerTest {

    @Test
    @DisplayName("TermsController deve ser criado")
    void deveCriarController() {

        TermsService service =
                mock(TermsService.class);

        TermsController controller =
                new TermsController(service);

        assertNotNull(controller);
    }

    @Test
    @DisplayName("Classe TermsController deve carregar")
    void classeDeveCarregar() {

        assertNotNull(TermsController.class);
    }
}