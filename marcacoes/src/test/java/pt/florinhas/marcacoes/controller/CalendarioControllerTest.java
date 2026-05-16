package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.service.CalendarioService;

class CalendarioControllerTest {

    @Test
    @DisplayName("CalendarioController deve ser criado")
    void deveCriarController() {

        CalendarioService calendarioService =
                mock(CalendarioService.class);

        UtilizadorRepository utilizadorRepository =
                mock(UtilizadorRepository.class);

        CalendarioController controller =
                new CalendarioController(
                        calendarioService,
                        utilizadorRepository
                );

        assertNotNull(controller);
    }

    @Test
    @DisplayName("Classe CalendarioController deve carregar")
    void classeDeveCarregar() {

        assertNotNull(CalendarioController.class);
    }
}