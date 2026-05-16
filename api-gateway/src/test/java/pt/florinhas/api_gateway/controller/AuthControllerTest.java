package pt.florinhas.api_gateway.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.util.ReflectionTestUtils;

import pt.florinhas.api_gateway.dto.*;
import pt.florinhas.api_gateway.security.JwtService;
import pt.florinhas.api_gateway.service.AuditClient;
import pt.florinhas.api_gateway.service.AuthService;
import pt.florinhas.api_gateway.service.AuthService.AuthResult;
import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;

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

                lenient().when(jwtService.generateToken(any())).thenReturn("mocked-token");
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
                                false);

                when(authService.getCurrentUserResponse(utente))
                                .thenReturn(Optional.of(response));

                ResponseEntity<AuthResponse> result = controller.getCurrentUser(utente);

                assertEquals(200, result.getStatusCode().value());
                assertNotNull(result.getBody());
        }

        @Test
        void logout_DeveLimparCookie() {
                Utente utente = new Utente();
                utente.setId(1L);
                utente.setNome("Teste");
                utente.setEmail("teste@teste.com");

                ServerHttpRequest request = MockServerHttpRequest.post("/api/auth/logout")
                                .build();

                ResponseEntity<Void> result = controller.logout(utente, request);

                assertEquals(200, result.getStatusCode().value());
                assertTrue(result.getHeaders().getFirst("Set-Cookie").contains("jwt="));
                verify(auditClient).logAsync(eq(1L), eq("Teste"), eq("LOGOUT"), eq("UTILIZADOR"), eq(1L), anyString(),
                                anyString());
        }

        @Test
        void getCurrentUser_DeveRetornar401QuandoVazio() {
                Utente utente = new Utente();

                when(authService.getCurrentUserResponse(utente))
                                .thenReturn(Optional.empty());

                ResponseEntity<AuthResponse> result = controller.getCurrentUser(utente);

                assertEquals(401, result.getStatusCode().value());
                assertNull(result.getBody());
        }

        @Test
        void loginFuncionario_DeveRetornarOkComCookie_QuandoDadosValidos() {
                LoginFuncionarioRequest requestDto = new LoginFuncionarioRequest("func@florinhas.pt", "password123");
                AuthResponse authResponse = new AuthResponse(2L, "func@florinhas.pt", "Secretaria 1", "SECRETARIA",
                                "987654321", "911111111", System.currentTimeMillis(), true, false);
                AuthResult authResult = new AuthResult(authResponse);

                when(authService.loginFuncionario(requestDto)).thenReturn(authResult);

                ServerHttpRequest request = MockServerHttpRequest.post("/api/auth/login/funcionario")
                                .header("X-Forwarded-For", "192.168.1.100, 10.0.0.1")
                                .build();

                ResponseEntity<AuthResponse> result = controller.loginFuncionario(requestDto, request);

                assertEquals(200, result.getStatusCode().value());
                assertNotNull(result.getBody());
                assertEquals("func@florinhas.pt", result.getBody().email());
                assertTrue(result.getHeaders().getFirst("Set-Cookie").contains("jwt=mocked-token"));
                verify(auditClient).logAsync(eq(2L), eq("Secretaria 1"), eq("LOGIN_FUNCIONARIO"), eq("UTILIZADOR"),
                                eq(2L), anyString(), eq("192.168.1.100"));
        }

        @Test
        void loginUtente_DeveRetornarOkComCookie_QuandoDadosValidos() {
                LoginUtenteRequest requestDto = new LoginUtenteRequest("123456789", "password123");
                AuthResponse authResponse = new AuthResponse(3L, "utente@florinhas.pt", "Utente 1", "UTENTE",
                                "123456789", "922222222", System.currentTimeMillis(), true, false);
                AuthResult authResult = new AuthResult(authResponse);

                when(authService.loginUtente(requestDto)).thenReturn(authResult);

                ServerHttpRequest request = MockServerHttpRequest.post("/api/auth/login/utente")
                                .header("X-Real-IP", "172.16.0.5")
                                .build();

                ResponseEntity<AuthResponse> result = controller.loginUtente(requestDto, request);

                assertEquals(200, result.getStatusCode().value());
                assertNotNull(result.getBody());
                assertEquals("utente@florinhas.pt", result.getBody().email());
                assertTrue(result.getHeaders().getFirst("Set-Cookie").contains("jwt=mocked-token"));
                verify(auditClient).logAsync(eq(3L), eq("Utente 1"), eq("LOGIN_UTENTE"), eq("UTILIZADOR"), eq(3L),
                                anyString(), eq("172.16.0.5"));
        }

        @Test
        void registerUtente_DeveRetornarOkComCookie_QuandoDadosValidos() {
                UtenteRegisterRequest requestDto = new UtenteRegisterRequest("Utente 1", "utente@florinhas.pt",
                                "password123", "123456789", "922222222", LocalDate.now().minusYears(20), true);
                AuthResponse authResponse = new AuthResponse(4L, "utente@florinhas.pt", "Utente 1", "UTENTE",
                                "123456789", "922222222", System.currentTimeMillis(), true, false);
                AuthResult authResult = new AuthResult(authResponse);

                when(authService.registerUtente(requestDto)).thenReturn(authResult);

                ServerHttpRequest request = MockServerHttpRequest.post("/api/auth/register/utente").build();

                ResponseEntity<AuthResponse> result = controller.registerUtente(requestDto, request);

                assertEquals(200, result.getStatusCode().value());
                assertNotNull(result.getBody());
                assertEquals("utente@florinhas.pt", result.getBody().email());
                assertTrue(result.getHeaders().getFirst("Set-Cookie").contains("jwt=mocked-token"));
                verify(auditClient).logAsync(eq(4L), eq("Utente 1"), eq("REGISTO_UTENTE"), eq("UTILIZADOR"), eq(4L),
                                anyString(), anyString());
        }

        @Test
        void registerFuncionario_DeveRetornarOkComCookie_QuandoDadosValidos() {
                FuncionarioRegisterRequest requestDto = new FuncionarioRegisterRequest("Secretaria 1",
                                "func@florinhas.pt", "password123", "987654321", "911111111", "SECRETARIA",
                                LocalDate.now().minusYears(30), true);
                AuthResponse authResponse = new AuthResponse(5L, "func@florinhas.pt", "Secretaria 1", "SECRETARIA",
                                "987654321", "911111111", System.currentTimeMillis(), true, false);
                AuthResult authResult = new AuthResult(authResponse);

                when(authService.registerFuncionario(requestDto)).thenReturn(authResult);

                ServerHttpRequest request = MockServerHttpRequest.post("/api/auth/register/funcionario").build();

                ResponseEntity<AuthResponse> result = controller.registerFuncionario(requestDto, request);

                assertEquals(200, result.getStatusCode().value());
                assertNotNull(result.getBody());
                assertEquals("func@florinhas.pt", result.getBody().email());
                assertTrue(result.getHeaders().getFirst("Set-Cookie").contains("jwt=mocked-token"));
                verify(auditClient).logAsync(eq(5L), eq("Secretaria 1"), eq("REGISTO_FUNCIONARIO"), eq("UTILIZADOR"),
                                eq(5L), anyString(), anyString());
        }

        @Test
        void updatePassword_DeveRetornarOk() {
                UpdatePasswordRequest requestDto = new UpdatePasswordRequest("newSecurePassword", true);
                Utilizador utilizador = new Funcionario();
                utilizador.setId(10L);

                ResponseEntity<Void> result = controller.updatePassword(requestDto, utilizador);

                assertEquals(200, result.getStatusCode().value());
                verify(authService).updatePassword(eq(10L), eq("newSecurePassword"), eq(true));
        }

        @Test
        void shouldUseSecureCookie_DeveRetornarSecure_QuandoHttpsOuForwardedProtoHttps() {
                ReflectionTestUtils.setField(controller, "secureCookies", true);

                LoginFuncionarioRequest requestDto = new LoginFuncionarioRequest("func@florinhas.pt", "password123");
                AuthResponse authResponse = new AuthResponse(2L, "func@florinhas.pt", "Secretaria 1", "SECRETARIA",
                                "987654321", "911111111", System.currentTimeMillis(), true, false);
                AuthResult authResult = new AuthResult(authResponse);

                when(authService.loginFuncionario(requestDto)).thenReturn(authResult);

                // HTTPS Scheme
                ServerHttpRequest requestHttps = MockServerHttpRequest
                                .post("https://florinhas.pt/api/auth/login/funcionario").build();
                ResponseEntity<AuthResponse> resultHttps = controller.loginFuncionario(requestDto, requestHttps);
                assertTrue(resultHttps.getHeaders().getFirst("Set-Cookie").contains("Secure"));

                // HTTP Scheme with X-Forwarded-Proto = https
                ServerHttpRequest requestProto = MockServerHttpRequest
                                .post("http://florinhas.pt/api/auth/login/funcionario")
                                .header("X-Forwarded-Proto", "https")
                                .build();
                ResponseEntity<AuthResponse> resultProto = controller.loginFuncionario(requestDto, requestProto);
                assertTrue(resultProto.getHeaders().getFirst("Set-Cookie").contains("Secure"));
        }
}