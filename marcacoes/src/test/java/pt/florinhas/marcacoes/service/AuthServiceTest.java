package pt.florinhas.marcacoes.service;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.FuncionarioTipo;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.dto.LoginUtenteRequest;
import pt.florinhas.marcacoes.dto.UtenteRegisterRequest;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.security.JwtService;
import pt.florinhas.marcacoes.validation.NifValidator;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UtilizadorRepository utilizadorRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private UtenteRepository utenteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private NifValidator nifValidator;

    @InjectMocks
    private AuthService authService;

    private LoginUtenteRequest loginUtenteRequest;
    private UtenteRegisterRequest utenteRegisterRequest;
    private Utente utente;
    private Funcionario funcionario;

    @BeforeEach
    void setUp() {
        loginUtenteRequest = new LoginUtenteRequest("123456789", "password123");

        utenteRegisterRequest = new UtenteRegisterRequest(
                "Test User",
                "test@example.com",
                "password123",
                "123456789",
                "912345678",
                LocalDate.now(),
                true);

        utente = new Utente();
        utente.setId(1L);
        utente.setEmail("test@example.com");
        utente.setNome("Test User");
        utente.setNif("123456789");
        utente.setPassHash("hashedPassword");
        utente.setActivo(true);

        funcionario = new Funcionario();
        funcionario.setId(2L);
        funcionario.setEmail("func@example.com");
        funcionario.setNome("Test Funcionario");
        funcionario.setPassHash("hashedPassword");
        funcionario.setTipo(FuncionarioTipo.SECRETARIA);
        funcionario.setActivo(true);
    }

    @Test
    void loginUtente_DeveRetornarAuthResponse_QuandoCredenciaisValidas() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(utilizadorRepository.findByNif("123456789"))
                .thenReturn(List.of(utente));
        when(jwtService.generateToken(any()))
                .thenReturn("jwt-token");

        // Act
        AuthService.AuthResult result = authService.loginUtente(loginUtenteRequest);

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token", result.token());
        assertEquals("test@example.com", result.response().email());
        assertEquals("UTENTE", result.response().role());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(utilizadorRepository).findByNif("123456789");
        verify(jwtService).generateToken(utente);
    }

    @Test
    void registerUtente_DeveCriarUtente_QuandoDadosValidos() {
        // Arrange
        when(utilizadorRepository.existsByEmail(anyString())).thenReturn(false);
        when(utilizadorRepository.existsByNif(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(utenteRepository.save(any(Utente.class))).thenReturn(utente);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        // Act
        AuthService.AuthResult result = authService.registerUtente(utenteRegisterRequest);

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token", result.token());
        assertEquals("UTENTE", result.response().role());
        verify(utenteRepository).save(any(Utente.class));
    }

    @Test
    void getCurrentUserId_DeveRetornarId_QuandoAutenticado() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(utente);

            Long userId = authService.getCurrentUserId();

            assertEquals(1L, userId);
        }
    }

    @Test
    void getCurrentUserId_DeveRetornarNull_QuandoNaoAutenticado() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            Long userId = authService.getCurrentUserId();

            assertNull(userId);
        }
    }

    @Test
    void isAdmin_DeveRetornarTrue_QuandoRoleSecretaria() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            doReturn(List.of(new SimpleGrantedAuthority("ROLE_SECRETARIA")))
                    .when(authentication).getAuthorities();

            boolean isAdmin = authService.isAdmin();

            assertTrue(isAdmin);
        }
    }

    @Test
    void isAdmin_DeveRetornarFalse_QuandoRoleFuncionario() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            doReturn(List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO")))
                    .when(authentication).getAuthorities();

            boolean isAdmin = authService.isAdmin();

            assertFalse(isAdmin);
        }
    }

    @Test
    void isAdmin_DeveRetornarFalse_QuandoRoleUtente() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            doReturn(List.of(new SimpleGrantedAuthority("ROLE_UTENTE")))
                    .when(authentication).getAuthorities();

            boolean isAdmin = authService.isAdmin();

            assertFalse(isAdmin);
        }
    }

    @Test
    void updatePassword_DeveAtivarFuncionarioCriadoPelaSecretaria() {
        // Arrange
        Funcionario funcionarioPendente = new Funcionario();
        funcionarioPendente.setId(10L);
        funcionarioPendente.setEmail("novo.funcionario@florinhasdovouga.pt");
        funcionarioPendente.setTipo(FuncionarioTipo.ESCOLA);
        funcionarioPendente.setActivo(false);
        funcionarioPendente.setTermsAcceptedAt(null);

        when(utilizadorRepository.findById(10L)).thenReturn(java.util.Optional.of(funcionarioPendente));
        when(passwordEncoder.encode("NovaPassword123")).thenReturn("hashed-new-password");

        // Act
        authService.updatePassword(10L, "NovaPassword123", true);

        // Assert
        assertTrue(funcionarioPendente.isActivo());
        assertNotNull(funcionarioPendente.getTermsAcceptedAt());
        assertEquals("hashed-new-password", funcionarioPendente.getPassHash());
        verify(funcionarioRepository).save(funcionarioPendente);
    }

    @Test
    void updatePassword_DeveFalharSemAceitarTermosQuandoNecessario() {
        // Arrange
        Funcionario funcionarioPendente = new Funcionario();
        funcionarioPendente.setId(11L);
        funcionarioPendente.setActivo(false);
        funcionarioPendente.setTermsAcceptedAt(null);

        when(utilizadorRepository.findById(11L)).thenReturn(java.util.Optional.of(funcionarioPendente));

        // Act + Assert
        assertThrows(RuntimeException.class, () -> authService.updatePassword(11L, "NovaPassword123", false));
        verify(funcionarioRepository, never()).save(any(Funcionario.class));
    }
}
