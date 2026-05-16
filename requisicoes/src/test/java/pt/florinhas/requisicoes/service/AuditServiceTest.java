package pt.florinhas.requisicoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;


import jakarta.servlet.http.HttpServletRequest;

class AuditServiceTest {

    private RestTemplate restTemplate;
    private HttpServletRequest request;

    private AuditService service;

    @BeforeEach
    void setUp() {

        restTemplate = mock(RestTemplate.class);

        request = mock(HttpServletRequest.class);

        when(request.getRemoteAddr())
                .thenReturn("127.0.0.1");

        service = new AuditService(
                restTemplate,
                "http://localhost",
                "secret",
                request);
    }

    @Test
    void log_NaoDeveLancarExcecao() {

        assertDoesNotThrow(() ->
                service.log(
                        "TESTE",
                        "ENTITY",
                        1L,
                        "Detalhes"));

        verify(restTemplate)
        .postForEntity(
                anyString(),
                any(),
                eq(Void.class));
    }
}