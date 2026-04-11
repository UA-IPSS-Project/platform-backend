package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.marcacoes.dto.AuthResponse;
import pt.florinhas.marcacoes.dto.FuncionarioRegisterRequest;
import pt.florinhas.marcacoes.dto.LoginFuncionarioRequest;
import pt.florinhas.marcacoes.dto.LoginUtenteRequest;
import pt.florinhas.marcacoes.dto.UpdatePasswordRequest;
import pt.florinhas.marcacoes.dto.UtenteRegisterRequest;
import pt.florinhas.marcacoes.service.AuthService;
import pt.florinhas.marcacoes.service.AuthService.AuthResult;
import pt.florinhas.marcacoes.service.UtilizadorService;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UtilizadorService utilizadorService;

    private AuthController authController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthController(authService, utilizadorService);
    }

    private AuthResponse buildResponse(String role, boolean active, boolean requiresPasswordSetup) {
        return new AuthResponse(
                1L,
                "user@test.com",
                "User Test",
                role,
                "100000002",
                "999999999",
                System.currentTimeMillis() + 1000,
                active,
                requiresPasswordSetup
        );
    }

    // =========================
    // LOGIN FUNCIONARIO
    // =========================

    @Test
    void loginFuncionario_DeveDelegarNoServiceERetornarOk() {
        LoginFuncionarioRequest request = new LoginFuncionarioRequest("func@test.com", "123456");
        AuthResponse response = buildResponse("SECRETARIA", true, false);

        when(authService.loginFuncionario(request))
                .thenReturn(new AuthResult(response));

        ResponseEntity<AuthResponse> result = authController.loginFuncionario(request);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("SECRETARIA", result.getBody().role());
        verify(authService).loginFuncionario(request);
    }

    // =========================
    // LOGIN UTENTE
    // =========================

    @Test
    void loginUtente_DeveDelegarNoServiceERetornarOk() {
        LoginUtenteRequest request = new LoginUtenteRequest("100000002", "123456");
        AuthResponse response = buildResponse("UTENTE", true, false);

        when(authService.loginUtente(request))
                .thenReturn(new AuthResult(response));

        ResponseEntity<AuthResponse> result = authController.loginUtente(request);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("UTENTE", result.getBody().role());
        verify(authService).loginUtente(request);
    }

    // =========================
    // REGISTER UTENTE
    // =========================

    @Test
    void registerUtente_DeveDelegarNoServiceERetornarOk() {
        UtenteRegisterRequest request = new UtenteRegisterRequest(
                "Utente",
                "utente@test.com",
                "123456",
                "100000002",
                "999999999",
                LocalDate.of(2000, 1, 1),
                true
        );

        AuthResponse response = buildResponse("UTENTE", true, false);

        when(authService.registerUtente(request))
                .thenReturn(new AuthResult(response));

        ResponseEntity<AuthResponse> result = authController.registerUtente(request);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("UTENTE", result.getBody().role());
        verify(authService).registerUtente(request);
    }

    // =========================
    // REGISTER FUNCIONARIO
    // =========================

    @Test
    void registerFuncionario_DeveDelegarNoServiceERetornarOk() {
        FuncionarioRegisterRequest request = new FuncionarioRegisterRequest(
                "Funcionario",
                "func@test.com",
                "123456",
                "100000002",
                "999999999",
                "SECRETARIA",
                LocalDate.of(1990, 1, 1),
                true
        );

        AuthResponse response = buildResponse("SECRETARIA", false, false);

        when(authService.registerFuncionario(request))
                .thenReturn(new AuthResult(response));

        ResponseEntity<AuthResponse> result = authController.registerFuncionario(request);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("SECRETARIA", result.getBody().role());
        verify(authService).registerFuncionario(request);
    }

    // =========================
    // UPDATE PASSWORD
    // =========================

    @Test
    void updatePassword_DeveDelegarNoServiceERetornarOk() {
        Utilizador utilizador = new Utente();
        utilizador.setId(10L);

        UpdatePasswordRequest request = new UpdatePasswordRequest("novaPassword123", true);

        ResponseEntity<Void> result = authController.updatePassword(request, utilizador);

        assertEquals(200, result.getStatusCode().value());
        verify(authService).updatePassword(10L, "novaPassword123", true);
    }

    // =========================
    // GET CURRENT USER
    // =========================

    @Test
    void getCurrentUser_DeveRetornar401_QuandoUtilizadorNull() {
        ResponseEntity<AuthResponse> result = authController.getCurrentUser(null);

        assertEquals(401, result.getStatusCode().value());
        assertNull(result.getBody());
    }

    @Test
    void getCurrentUser_DeveRetornarDadosDeUtente() {
        Utente principal = new Utente();
        principal.setId(1L);
        principal.setEmail("utente@test.com");
        principal.setNome("Utente");
        principal.setNif("100000002");
        principal.setTelefone("999999999");

        Utente persisted = new Utente();
        persisted.setId(1L);
        persisted.setEmail("utente@test.com");
        persisted.setNome("Utente");
        persisted.setNif("100000002");
        persisted.setTelefone("999999999");
        persisted.setActivo(true);
        persisted.setTermsAcceptedAt(LocalDateTime.now());

        when(utilizadorService.obterUtilizadorPorId(1L)).thenReturn(persisted);
        when(authService.requiresPasswordSetup(persisted, true)).thenReturn(false);

        ResponseEntity<AuthResponse> result = authController.getCurrentUser(principal);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().id());
        assertEquals("utente@test.com", result.getBody().email());
        assertEquals("Utente", result.getBody().nome());
        assertTrue(result.getBody().active());
        assertFalse(result.getBody().requiresPasswordSetup());

        verify(utilizadorService).obterUtilizadorPorId(1L);
        verify(authService).requiresPasswordSetup(persisted, true);
    }

    @Test
    void getCurrentUser_DeveRetornarDadosDeFuncionario() {
        Funcionario principal = new Funcionario();
        principal.setId(2L);
        principal.setEmail("func@test.com");
        principal.setNome("Funcionario");
        principal.setNif("200000001");
        principal.setTelefone("911111111");
        principal.setTipo(FuncionarioTipo.SECRETARIA);

        Funcionario persisted = new Funcionario();
        persisted.setId(2L);
        persisted.setEmail("func@test.com");
        persisted.setNome("Funcionario");
        persisted.setNif("200000001");
        persisted.setTelefone("911111111");
        persisted.setTipo(FuncionarioTipo.SECRETARIA);
        persisted.setActivo(false);

        when(utilizadorService.obterUtilizadorPorId(2L)).thenReturn(persisted);
        when(authService.requiresPasswordSetup(persisted, false)).thenReturn(true);

        ResponseEntity<AuthResponse> result = authController.getCurrentUser(principal);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(2L, result.getBody().id());
        assertEquals("func@test.com", result.getBody().email());
        assertFalse(result.getBody().active());
        assertTrue(result.getBody().requiresPasswordSetup());

        verify(utilizadorService).obterUtilizadorPorId(2L);
        verify(authService).requiresPasswordSetup(persisted, false);
    }

    // =========================
    // LOGOUT
    // =========================

    @Test
    void logout_DeveRetornarOk() {
        ResponseEntity<Void> result = authController.logout();

        assertEquals(200, result.getStatusCode().value());
    }
}