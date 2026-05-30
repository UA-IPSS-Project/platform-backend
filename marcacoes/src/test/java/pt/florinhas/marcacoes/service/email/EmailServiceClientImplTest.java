package pt.florinhas.marcacoes.service.email;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class EmailServiceClientImplTest {

    private EmailServiceClientImpl service;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() throws Exception {

        service = new EmailServiceClientImpl();

        restTemplate = mock(RestTemplate.class);

        setField("notificacoesUrl", "http://localhost");
        setField("gatewaySecret", "secret");
        setField("restTemplate", restTemplate);
    }

    @Test
    void sendPassword_DeveEnviarPedido() {

        service.sendPassword("teste@test.com", "123");

        verify(restTemplate).postForObject(
            anyString(),
            any(),
            any(Class.class));
    }

    @Test
    void sendAppointmentCreated_DeveEnviarPedido() {

        service.sendAppointmentCreated(
                "teste@test.com",
                LocalDateTime.now(),
                1L,
                "Consulta",
                15);

        verify(restTemplate).postForObject(
            anyString(),
            any(),
            any(Class.class));
    }

    @Test
    void sendAppointmentCancelled_DeveEnviarPedido() {

        service.sendAppointmentCancelled(
                "teste@test.com",
                "Nuno",
                LocalDateTime.now(),
                "Consulta",
                "Motivo");

        verify(restTemplate).postForObject(
            anyString(),
            any(),
            any(Class.class));
    }

    @Test
    void sendAppointmentReminderOneDay_DeveEnviarPedido() {

        service.sendAppointmentReminderOneDay(
                "teste@test.com",
                LocalDateTime.now(),
                "Consulta");

        verify(restTemplate).postForObject(
            anyString(),
            any(),
            any(Class.class));
    }

    @Test
    void sendGenericEmail_DeveEnviarPedido() {

        service.sendGenericEmail(
                "teste@test.com",
                "Assunto",
                "Mensagem");

        verify(restTemplate).postForObject(
                anyString(),
                any(),
                any(Class.class));
    }

    @Test
    void sendEmailWithAttachment_DeveEnviarPedido() {

        service.sendEmailWithAttachment(
                "teste@test.com",
                "Assunto",
                "Mensagem",
                "abc".getBytes(),
                "teste.pdf");

        verify(restTemplate).postForObject(
            anyString(),
            any(),
            any(Class.class));
    }

    @Test
    void sendGenericEmail_NaoDeveLancarErro() {

        when(restTemplate.postForObject(
                anyString(),
                any(),
                any()))
                .thenThrow(new RuntimeException());

        assertDoesNotThrow(() ->
                service.sendGenericEmail(
                        "teste@test.com",
                        "Assunto",
                        "Mensagem"));
    }

    private void setField(String field, Object value) throws Exception {

        Field f =
                EmailServiceClientImpl.class.getDeclaredField(field);

        f.setAccessible(true);
        f.set(service, value);
    }
}