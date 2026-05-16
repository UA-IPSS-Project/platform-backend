package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.florinhas.marcacoes.service.email.EmailService;

class ReportControllerTest {

    @Test
    @DisplayName("ReportController deve ser criado")
    void deveCriarController() {

        EmailService emailService =
                mock(EmailService.class);

        ReportController controller =
                new ReportController(emailService);

        assertNotNull(controller);
    }

    @Test
    @DisplayName("Classe ReportController deve carregar")
    void classeDeveCarregar() {

        assertNotNull(ReportController.class);
    }
}