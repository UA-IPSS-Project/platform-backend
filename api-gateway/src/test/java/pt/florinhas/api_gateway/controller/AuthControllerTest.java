package pt.florinhas.api_gateway.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import pt.florinhas.api_gateway.dto.AuthResponse;
import pt.florinhas.api_gateway.dto.LoginFuncionarioRequest;
import pt.florinhas.api_gateway.security.JwtService;
import pt.florinhas.api_gateway.service.AuditClient;
import pt.florinhas.api_gateway.service.AuthService;
import pt.florinhas.api_gateway.service.AuthService.AuthResult;
import pt.florinhas.common_data.domain.Utilizador;

class AuthControllerTest {

    private AuthService authService;
    private JwtService jwtService;
    private AuditClient auditClient;

    private AuthController controller;

    @BeforeEach
    void setUp() throws Exception {

        authService = mock(AuthService.class);
        jwtService = mock(JwtService.class);
        auditClient = mock(AuditClient.class);

        controller = new AuthController(
                authService,
                jwtService,
                auditClient);

        setField("secureCookies", false);
        setField("cookieSameSite", "Lax");
        setField("jwtExpiration", 86400000L);
    }

    @Test
    void loginFuncionario_DeveRetornarCookieJwt() {

        AuthResponse response = new AuthResponse(
                1L,
                "teste@teste.com",
                "Teste",
                "SECRETARIA",
                "123456789",
                "912345678",
                System.currentTimeMillis(),
                true,
                false);

        when(authService.loginFuncionario(any()))
                .thenReturn(new AuthResult(response));

        when(jwtService.generateToken(any()))
                .thenReturn("jwt-token");

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .post("/api/auth/login/funcionario")
                        .build();

        ResponseEntity<AuthResponse> result =
                controller.loginFuncionario(
                        new LoginFuncionarioRequest(
                                "teste@teste.com",
                                "123"),
                        request);

        assertEquals(
                200,
                result.getStatusCode().value());

        assertEquals(
                response,
                result.getBody());

        assertNotNull(
                result.getHeaders()
                        .getFirst(HttpHeaders.SET_COOKIE));
    }

    @Test
    void getCurrentUser_DeveRetornar401() {

        when(authService.getCurrentUserResponse(null))
                .thenReturn(Optional.empty());

        ResponseEntity<AuthResponse> result =
                controller.getCurrentUser(null);

        assertEquals(
                401,
                result.getStatusCode().value());
    }

    @Test
    void getCurrentUser_DeveRetornarUtilizador() {

        AuthResponse response = new AuthResponse(
                1L,
                "teste@teste.com",
                "Teste",
                "UTENTE",
                "123456789",
                "912345678",
                System.currentTimeMillis(),
                true,
                false);

        Utilizador utilizador =
                mock(Utilizador.class);

        when(authService.getCurrentUserResponse(utilizador))
                .thenReturn(Optional.of(response));

        ResponseEntity<AuthResponse> result =
                controller.getCurrentUser(utilizador);

        assertEquals(
                200,
                result.getStatusCode().value());

        assertEquals(
                response,
                result.getBody());
    }

    @Test
    void logout_DeveLimparCookie() {

        Utilizador utilizador =
                mock(Utilizador.class);

        when(utilizador.getId())
                .thenReturn(1L);

        when(utilizador.getNome())
                .thenReturn("Teste");

        when(utilizador.getEmail())
                .thenReturn("teste@teste.com");

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .post("/api/auth/logout")
                        .build();

        ResponseEntity<Void> result =
                controller.logout(
                        utilizador,
                        request);

        assertEquals(
                200,
                result.getStatusCode().value());

        assertNotNull(
                result.getHeaders()
                        .getFirst(HttpHeaders.SET_COOKIE));

        verify(auditClient)
                .logAsync(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any());
    }

    private void setField(
            String fieldName,
            Object value
    ) throws Exception {

        Field field =
                AuthController.class
                        .getDeclaredField(fieldName);

        field.setAccessible(true);

        field.set(controller, value);
    }

    @Test
        void loginFuncionario_DeveConfigurarCookieLaxSemSecure()
                throws Exception {

        AuthResponse response =
                new AuthResponse(
                        1L,
                        "teste@teste.com",
                        "Teste",
                        "SECRETARIA",
                        "123456789",
                        "912345678",
                        System.currentTimeMillis(),
                        true,
                        false);

        when(authService.loginFuncionario(any()))
                .thenReturn(new AuthResult(response));

        when(jwtService.generateToken(any()))
                .thenReturn("jwt-token");

        setField("secureCookies", false);
        setField("cookieSameSite", "Lax");

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .post("/api/auth/login/funcionario")
                        .build();

        ResponseEntity<AuthResponse> result =
                controller.loginFuncionario(
                        new LoginFuncionarioRequest(
                                "teste@teste.com",
                                "123"),
                        request);

        String cookie =
                result.getHeaders()
                        .getFirst(HttpHeaders.SET_COOKIE);

        assertNotNull(cookie);

        assertEquals(true, cookie.contains("HttpOnly"));

        assertEquals(true, cookie.contains("SameSite=Lax"));

        assertEquals(false, cookie.contains("Secure"));
        }

        @Test
        void loginFuncionario_DeveConfigurarCookieSecure()
                throws Exception {

        AuthResponse response =
                new AuthResponse(
                        1L,
                        "teste@teste.com",
                        "Teste",
                        "SECRETARIA",
                        "123456789",
                        "912345678",
                        System.currentTimeMillis(),
                        true,
                        false);

        when(authService.loginFuncionario(any()))
                .thenReturn(new AuthResult(response));

        when(jwtService.generateToken(any()))
                .thenReturn("jwt-token");

        setField("secureCookies", true);
        setField("cookieSameSite", "Strict");

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .post("/api/auth/login/funcionario")
                        .header("X-Forwarded-Proto", "https")
                        .build();

        ResponseEntity<AuthResponse> result =
                controller.loginFuncionario(
                        new LoginFuncionarioRequest(
                                "teste@teste.com",
                                "123"),
                        request);

        String cookie =
                result.getHeaders()
                        .getFirst(HttpHeaders.SET_COOKIE);

        assertNotNull(cookie);

        assertEquals(true, cookie.contains("HttpOnly"));

        assertEquals(true, cookie.contains("Secure"));

        assertEquals(true, cookie.contains("SameSite=Strict"));
        }

        @Test
        void loginFuncionario_DevePropagarErro() {

        when(authService.loginFuncionario(any()))
                .thenThrow(
                        new pt.florinhas.common_data.exception.BadRequestException(
                                "credenciais inválidas"));

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .post("/api/auth/login/funcionario")
                        .build();

        var exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        pt.florinhas.common_data.exception.BadRequestException.class,
                        () -> controller.loginFuncionario(
                                new LoginFuncionarioRequest(
                                        "teste@teste.com",
                                        "123"),
                                request));

        assertEquals("credenciais inválidas", exception.getMessage());
        }
}