package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UtilizadorControllerTest {

    @Test
    @DisplayName("UtilizadorController deve ser criado")
    void deveCriarController() {

        UtilizadorController controller =
                new UtilizadorController();

        assertNotNull(controller);
    }

    @Test
    @DisplayName("Classe UtilizadorController deve carregar")
    void classeDeveCarregar() {

        assertNotNull(UtilizadorController.class);
    }
}