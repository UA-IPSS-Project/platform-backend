package pt.florinhas.marcacoes.service.email;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

class EmailServiceClientImplTest {

    @Test
    @DisplayName("Deve criar EmailServiceClientImpl")
    void deveCriarEmailServiceClientImpl() {

        EmailServiceClientImpl service =
                new EmailServiceClientImpl();

        assertNotNull(service);
    }

    @Test
    @DisplayName("sendPassword não deve lançar exceção")
    void sendPassword_NaoDeveLancarException() {

        EmailServiceClientImpl service =
                new EmailServiceClientImpl();

        ReflectionTestUtils.setField(
                service,
                "notificacoesUrl",
                "http://localhost"
        );

        assertDoesNotThrow(
                () -> service.sendPassword(
                        "teste@test.com",
                        "123"
                )
        );
    }

    @Test
    @DisplayName("sendGenericEmail não deve lançar exceção")
    void sendGenericEmail_NaoDeveLancarException() {

        EmailServiceClientImpl service =
                new EmailServiceClientImpl();

        ReflectionTestUtils.setField(
                service,
                "notificacoesUrl",
                "http://localhost"
        );

        assertDoesNotThrow(
                () -> service.sendGenericEmail(
                        "teste@test.com",
                        "Teste",
                        "Mensagem"
                )
        );
    }
}