package pt.florinhas.requisicoes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import pt.florinhas.common_data.domain.Utilizador;

class AuditServiceTest {

    private RestTemplate restTemplate;

    private HttpServletRequest request;

    private AuditService service;

    @BeforeEach
    void setUp() {

        restTemplate =
                mock(RestTemplate.class);

        request =
                mock(HttpServletRequest.class);

        service =
                new AuditService(
                        restTemplate,
                        "http://localhost",
                        "secret",
                        request);
    }

    @AfterEach
    void clearSecurity() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void log_DeveEnviarAuditoria() {

        Utilizador user =
                new Utilizador();

        user.setId(1L);
        user.setNome("Nuno");

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null));

        when(request.getRemoteAddr())
                .thenReturn("127.0.0.1");

        service.log(
                "CREATE",
                "REQUISICAO",
                1L,
                "Detalhes");

        verify(restTemplate)
                .postForEntity(
                        any(String.class),
                        any(HttpEntity.class),
                        any(Class.class));
    }

    @Test
    void log_DeveUsarNomeAuthentication() {

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "nuno@test.com",
                                null));

        when(request.getRemoteAddr())
                .thenReturn("127.0.0.1");

        service.log(
                "UPDATE",
                "REQUISICAO",
                1L,
                "Detalhes");

        verify(restTemplate)
                .postForEntity(
                        any(String.class),
                        any(HttpEntity.class),
                        any(Class.class));
    }

    @Test
    void log_DeveIgnorarErros() {

        when(request.getRemoteAddr())
                .thenThrow(new RuntimeException());

        assertDoesNotThrow(() ->
                service.log(
                        "DELETE",
                        "REQUISICAO",
                        1L,
                        "Detalhes"));
    }
}