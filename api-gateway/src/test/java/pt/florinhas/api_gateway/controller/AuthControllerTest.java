package pt.florinhas.api_gateway.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import pt.florinhas.api_gateway.dto.AuthResponse;
import pt.florinhas.api_gateway.security.JwtService;
import pt.florinhas.api_gateway.service.AuditClient;
import pt.florinhas.api_gateway.service.AuthService;
import pt.florinhas.common_data.domain.Utente;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditClient auditClient;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(
                authService,
                jwtService,
                auditClient);

        ReflectionTestUtils.setField(controller, "jwtExpiration", 86400000L);
        ReflectionTestUtils.setField(controller, "cookieSameSite", "Lax");
        ReflectionTestUtils.setField(controller, "secureCookies", false);
    }

    @Test
    void getCurrentUser_DeveRetornarUser() {

        Utente utente = new Utente();
        utente.setId(1L);
        utente.setNome("Teste");

        AuthResponse response = new AuthResponse(
                1L,
                "teste@teste.com",
                "Teste",
                "UTENTE",
                "123456789",
                "912345678",
                System.currentTimeMillis(),
                true,
                false
        );

        when(authService.getCurrentUserResponse(utente))
                .thenReturn(Optional.of(response));

        ResponseEntity<AuthResponse> result =
                controller.getCurrentUser(utente);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
    }

    @Test
    void logout_DeveLimparCookie() {

        Utente utente = new Utente();
        utente.setId(1L);
        utente.setNome("Teste");
        utente.setEmail("teste@teste.com");

        ServerHttpRequest request =
                MockServerHttpRequest.post("/api/auth/logout")
                        .build();

        ResponseEntity<Void> result =
                controller.logout(utente, request);

        assertEquals(200, result.getStatusCode().value());
        assertTrue(
                result.getHeaders()
                        .getFirst("Set-Cookie")
                        .contains("jwt=")
        );
    }
}