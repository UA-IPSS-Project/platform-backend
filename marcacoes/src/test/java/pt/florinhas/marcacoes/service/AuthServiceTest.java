package pt.florinhas.marcacoes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.repository.UtenteRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.dto.*;
import pt.florinhas.marcacoes.exception.BadRequestException;
import pt.florinhas.marcacoes.validation.NifValidator;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UtilizadorRepository utilizadorRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private UtenteRepository utenteRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private NifValidator nifValidator;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setup() {
        // evita expiresAt = 0
        authService = new AuthService(
                utilizadorRepository,
                funcionarioRepository,
                utenteRepository,
                passwordEncoder,
                authenticationManager,
                nifValidator
        );
    }

    // =========================
    // LOGIN FUNCIONARIO
    // =========================

    @Test
    void loginFuncionario_DeveFalhar_QuandoNaoExisteUser() {
        var request = new LoginFuncionarioRequest("a@a.com", "123");

        when(utilizadorRepository.findByEmail("a@a.com"))
                .thenReturn(List.of());

        assertThrows(BadRequestException.class,
                () -> authService.loginFuncionario(request));
    }

    @Test
    void loginFuncionario_DeveFalhar_QuandoNaoEhFuncionario() {
        var request = new LoginFuncionarioRequest("a@a.com", "123");

        Utilizador user = new Utente(); // não é funcionário

        when(utilizadorRepository.findByEmail("a@a.com"))
                .thenReturn(List.of(user));

        assertThrows(BadRequestException.class,
                () -> authService.loginFuncionario(request));
    }

    @Test
    void loginFuncionario_DeveFalhar_QuandoInativoEAguardandoAprovacao() {
        var request = new LoginFuncionarioRequest("a@a.com", "123");

        Funcionario f = new Funcionario();
        f.setActivo(false);
        f.setTermsAcceptedAt(java.time.LocalDateTime.now());

        when(utilizadorRepository.findByEmail("a@a.com"))
                .thenReturn(List.of(f));

        assertThrows(BadRequestException.class,
                () -> authService.loginFuncionario(request));
    }

    @Test
    void loginFuncionario_ComSucesso() {
        var request = new LoginFuncionarioRequest("a@a.com", "123");

        Funcionario f = new Funcionario();
        f.setId(1L);
        f.setEmail("a@a.com");
        f.setNome("Funcionario");
        f.setActivo(true);
        f.setTipo(FuncionarioTipo.SECRETARIA);

        when(utilizadorRepository.findByEmail("a@a.com"))
                .thenReturn(List.of(f));

        var result = authService.loginFuncionario(request);

        assertNotNull(result);
        assertEquals("Funcionario", result.response().nome());
        assertEquals("SECRETARIA", result.response().role());
    }

    // =========================
    // LOGIN UTENTE
    // =========================

    @Test
    void loginUtente_DeveFalhar_QuandoNaoExiste() {
        var request = new LoginUtenteRequest("123456789", "123");

        when(utilizadorRepository.findByNif("123456789"))
                .thenReturn(List.of());

        assertThrows(BadRequestException.class,
                () -> authService.loginUtente(request));
    }

    @Test
    void loginUtente_ComSucesso() {
        var request = new LoginUtenteRequest("123456789", "123");

        Utente u = new Utente();
        u.setId(1L);
        u.setNome("Utente");
        u.setEmail("u@u.com");
        u.setActivo(true);

        when(utilizadorRepository.findByNif("123456789"))
                .thenReturn(List.of(u));

        var result = authService.loginUtente(request);

        assertNotNull(result);
        assertEquals("Utente", result.response().nome());
        assertEquals("UTENTE", result.response().role());
    }

    // =========================
    // REGISTER UTENTE
    // =========================

    @Test
    void registerUtente_DeveFalhar_QuandoNaoAceitaTermos() {
        var req = new UtenteRegisterRequest(
                "Nome", "a@a.com", "123456",
                "123456789", null,
                LocalDate.now(), false
        );

        assertThrows(BadRequestException.class,
                () -> authService.registerUtente(req));
    }

    @Test
    void registerUtente_ComSucesso() {
        var req = new UtenteRegisterRequest(
                "Nome", "a@a.com", "123456",
                "123456789", null,
                LocalDate.now(), true
        );

        when(utilizadorRepository.existsByEmail("a@a.com")).thenReturn(false);
        when(utilizadorRepository.existsByNif("123456789")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("HASH");

        when(utenteRepository.save(any(Utente.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var result = authService.registerUtente(req);

        assertNotNull(result);
        assertEquals("UTENTE", result.response().role());
    }

    // =========================
    // REGISTER FUNCIONARIO
    // =========================

    @Test
    void registerFuncionario_DeveFalhar_QuandoFuncaoInvalida() {
        var req = new FuncionarioRegisterRequest(
                "Nome", "a@a.com", "123456",
                "123456789", null,
                "INVALIDA",
                LocalDate.now(), true
        );

        when(utilizadorRepository.existsByEmail("a@a.com")).thenReturn(false);
        when(utilizadorRepository.existsByNif("123456789")).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> authService.registerFuncionario(req));
    }

    @Test
    void registerFuncionario_ComSucesso() {
        var req = new FuncionarioRegisterRequest(
                "Nome", "a@a.com", "123456",
                "123456789", null,
                "SECRETARIA",
                LocalDate.now(), true
        );

        when(utilizadorRepository.existsByEmail("a@a.com")).thenReturn(false);
        when(utilizadorRepository.existsByNif("123456789")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("HASH");

        when(funcionarioRepository.save(any(Funcionario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var result = authService.registerFuncionario(req);

        assertNotNull(result);
        assertEquals("SECRETARIA", result.response().role());
    }

    // =========================
    // UPDATE PASSWORD
    // =========================

    @Test
    void updatePassword_DeveFalhar_QuandoNaoAceitaTermos() {
        Utente u = new Utente();
        u.setId(1L);
        u.setTermsAcceptedAt(null);

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(u));

        assertThrows(BadRequestException.class,
                () -> authService.updatePassword(1L, "nova", false));
    }

    @Test
    void updatePassword_ComSucesso() {
        Utente u = new Utente();
        u.setId(1L);
        u.setTermsAcceptedAt(null);

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(u));
        when(passwordEncoder.encode(any())).thenReturn("HASH");

        authService.updatePassword(1L, "nova", true);

        assertNotNull(u.getTermsAcceptedAt());
        assertTrue(u.isActivo());
    }

    // =========================
    // UTILS
    // =========================

    @Test
    void requiresPasswordSetup_DeveRetornarTrue() {
        Utilizador u = new Utente();
        u.setTermsAcceptedAt(null);

        assertTrue(authService.requiresPasswordSetup(u, false));
    }

    @Test
    void requiresPasswordSetup_DeveRetornarFalse() {
        Utilizador u = new Utente();
        u.setTermsAcceptedAt(java.time.LocalDateTime.now());

        assertFalse(authService.requiresPasswordSetup(u, true));
    }
}