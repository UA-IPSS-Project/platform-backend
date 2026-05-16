package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.service.AuthorizationService;
import pt.florinhas.marcacoes.service.MarcacaoService;

class MarcacaoControllerTest {

    @Test
    @DisplayName("MarcacaoController deve ser criado")
    void deveCriarController() {

        MarcacaoService marcacaoService =
                mock(MarcacaoService.class);

        AuthorizationService authorizationService =
                mock(AuthorizationService.class);

        MarcacaoController controller =
                new MarcacaoController(
                        marcacaoService,
                        authorizationService
                );

        assertNotNull(controller);
    }

    @Test
    @DisplayName("Classe MarcacaoController deve carregar")
    void classeDeveCarregar() {

        assertNotNull(MarcacaoController.class);
    }
}