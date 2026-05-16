package pt.florinhas.marcacoes.service.email;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailServiceTest {

    @Test
    @DisplayName("EmailService deve poder ser mockado")
    void emailService_DevePoderSerMockado() {

        EmailService service =
                mock(EmailService.class);

        assertNotNull(service);
    }

    @Test
    @DisplayName("Classe EmailService deve carregar")
    void classeDeveCarregar() {

        assertNotNull(EmailService.class);
    }
}