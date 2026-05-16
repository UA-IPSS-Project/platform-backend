package pt.florinhas.api_gateway.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import pt.florinhas.api_gateway.dto.*;
import pt.florinhas.common_data.domain.*;
import pt.florinhas.common_data.exception.BadRequestException;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.repository.UtenteRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.security.CryptoUtils;
import pt.florinhas.common_data.validation.NifValidator;

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
    private AuthenticationManager authenticationManager;

    @Mock
    private NifValidator nifValidator;

    @Mock
    private CryptoUtils cryptoUtils;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                utilizadorRepository,
                funcionarioRepository,
                utenteRepository,
                passwordEncoder,
                authenticationManager,
                nifValidator,
                cryptoUtils);

        ReflectionTestUtils.setField(authService, "jwtExpiration", 86400000L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==========================================
    // LOGIN FUNCIONARIO TESTS
    // ==========================================

    @Test
    void loginFuncionario_Sucesso_Ativo() {
        LoginFuncionarioRequest request = new LoginFuncionarioRequest("func@teste.com", "pass123");
        Funcionario funcionario = new Funcionario();
        funcionario.setId(10L);
        funcionario.setEmail("func@teste.com");
        funcionario.setNome("Funcionario Teste");
        funcionario.setActivo(true);
        funcionario.setTipo(FuncionarioTipo.SECRETARIA);
        funcionario.setNif("123456789");

        when(utilizadorRepository.findByEmail("func@teste.com")).thenReturn(List.of(funcionario));
        when(authenticationManager.authenticate(any())).thenReturn(null);

        AuthService.AuthResult result = authService.loginFuncionario(request);

        assertNotNull(result);
        assertEquals("SECRETARIA", result.response().role());
        assertTrue(result.response().active());
        assertFalse(result.response().requiresPasswordSetup());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void loginFuncionario_Sucesso_InativoSemTermos_PermiteAutenticarPrimeiroLogin() {
        LoginFuncionarioRequest request = new LoginFuncionarioRequest("func@teste.com", "pass123");
        Funcionario funcionario = new Funcionario();
        funcionario.setId(10L);
        funcionario.setEmail("func@teste.com");
        funcionario.setNome("Funcionario Teste");
        funcionario.setActivo(false);
        funcionario.setTermsAcceptedAt(null); // Sem termos aceites ainda
        funcionario.setTipo(null);
        funcionario.setNif("123456789");

        when(utilizadorRepository.findByEmail("func@teste.com")).thenReturn(List.of(funcionario));
        when(authenticationManager.authenticate(any())).thenReturn(null);

        AuthService.AuthResult result = authService.loginFuncionario(request);

        assertNotNull(result);
        assertEquals("FUNCIONARIO", result.response().role());
        assertFalse(result.response().active());
        assertTrue(result.response().requiresPasswordSetup());
    }

    @Test
    void loginFuncionario_DeveLancarErroQuandoUtilizadorNaoEncontrado() {
        LoginFuncionarioRequest request = new LoginFuncionarioRequest("func@teste.com", "pass123");
        when(utilizadorRepository.findByEmail("func@teste.com")).thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, () -> authService.loginFuncionario(request));
    }

    @Test
    void loginFuncionario_DeveLancarErroQuandoNaoForFuncionario() {
        LoginFuncionarioRequest request = new LoginFuncionarioRequest("func@teste.com", "pass123");
        Utente utente = new Utente();
        utente.setEmail("func@teste.com");

        when(utilizadorRepository.findByEmail("func@teste.com")).thenReturn(List.of(utente));

        assertThrows(BadRequestException.class, () -> authService.loginFuncionario(request));
    }

    @Test
    void loginFuncionario_DeveLancarErroQuandoInativoETermosAceites() {
        LoginFuncionarioRequest request = new LoginFuncionarioRequest("func@teste.com", "pass123");
        Funcionario funcionario = new Funcionario();
        funcionario.setActivo(false);
        funcionario.setTermsAcceptedAt(LocalDateTime.now()); // Auto-registo pendente de aprovação

        when(utilizadorRepository.findByEmail("func@teste.com")).thenReturn(List.of(funcionario));

        assertThrows(BadRequestException.class, () -> authService.loginFuncionario(request));
    }

    @Test
    void loginFuncionario_DeveLancarErroQuandoAuthenticationManagerFalhar() {
        LoginFuncionarioRequest request = new LoginFuncionarioRequest("func@teste.com", "pass123");
        Funcionario funcionario = new Funcionario();
        funcionario.setEmail("func@teste.com");
        funcionario.setActivo(true);

        when(utilizadorRepository.findByEmail("func@teste.com")).thenReturn(List.of(funcionario));
        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("Bad credentials"));

        assertThrows(BadRequestException.class, () -> authService.loginFuncionario(request));
    }

    // ==========================================
    // LOGIN UTENTE TESTS
    // ==========================================

    @Test
    void loginUtente_Sucesso() {
        LoginUtenteRequest request = new LoginUtenteRequest("123456789", "pass123");
        Utente utente = new Utente();
        utente.setId(20L);
        utente.setNif("123456789");
        utente.setEmail("utente@teste.com");
        utente.setNome("Utente Teste");
        utente.setActivo(true);

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(cryptoUtils.generateBlindIndex("123456789")).thenReturn("blind_index");
        when(utilizadorRepository.findByNifHash("blind_index")).thenReturn(List.of(utente));

        AuthService.AuthResult result = authService.loginUtente(request);

        assertNotNull(result);
        assertEquals("UTENTE", result.response().role());
        assertTrue(result.response().active());
    }

    @Test
    void loginUtente_DeveLancarErroQuandoAuthenticationFalhar() {
        LoginUtenteRequest request = new LoginUtenteRequest("123456789", "pass123");
        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("Bad credentials"));

        assertThrows(BadRequestException.class, () -> authService.loginUtente(request));
    }

    @Test
    void loginUtente_DeveLancarErroQuandoNaoEncontrado() {
        LoginUtenteRequest request = new LoginUtenteRequest("123456789", "pass123");
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(cryptoUtils.generateBlindIndex("123456789")).thenReturn("blind_index");
        when(utilizadorRepository.findByNifHash("blind_index")).thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, () -> authService.loginUtente(request));
    }

    @Test
    void loginUtente_DeveLancarErroQuandoNaoForInstanciaDeUtente() {
        LoginUtenteRequest request = new LoginUtenteRequest("123456789", "pass123");
        Funcionario funcionario = new Funcionario();

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(cryptoUtils.generateBlindIndex("123456789")).thenReturn("blind_index");
        when(utilizadorRepository.findByNifHash("blind_index")).thenReturn(List.of(funcionario));

        assertThrows(BadRequestException.class, () -> authService.loginUtente(request));
    }

    // ==========================================
    // REGISTER UTENTE TESTS
    // ==========================================

    @Test
    void registerUtente_Sucesso() {
        UtenteRegisterRequest request = new UtenteRegisterRequest(
                "Utente Novo",
                "novo@utente.com",
                "pass123",
                "123456789",
                "912345678",
                LocalDate.now(),
                true);

        when(utilizadorRepository.existsByEmail("novo@utente.com")).thenReturn(false);
        when(cryptoUtils.generateBlindIndex("123456789")).thenReturn("blind_index");
        when(utilizadorRepository.existsByNifHash("blind_index")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed_pass");
        when(utenteRepository.save(any(Utente.class))).thenAnswer(invocation -> {
            Utente saved = invocation.getArgument(0);
            saved.setId(30L);
            return saved;
        });

        AuthService.AuthResult result = authService.registerUtente(request);

        assertNotNull(result);
        assertEquals("UTENTE", result.response().role());
        assertTrue(result.response().active());
        verify(nifValidator).validateRequiredOrThrow("123456789");
    }

    @Test
    void registerUtente_DeveLancarErroQuandoTermosNaoAceites() {
        UtenteRegisterRequest request = new UtenteRegisterRequest(
                "Utente Novo",
                "novo@utente.com",
                "pass123",
                "123456789",
                "912345678",
                LocalDate.now(),
                false);

        assertThrows(BadRequestException.class, () -> authService.registerUtente(request));
    }

    // ==========================================
    // REGISTER FUNCIONARIO TESTS
    // ==========================================

    @Test
    void registerFuncionario_Sucesso_ComFuncoesDiferentes() {
        String[] funcoes = { "SECRETARIA", "SECRETÁRIA", "BALNEARIO", "BALNEÁRIO SOCIAL", "ESCOLA", "INTERNO",
                "SERVIÇOS INTERNOS" };
        FuncionarioTipo[] tiposEsperados = {
                FuncionarioTipo.SECRETARIA, FuncionarioTipo.SECRETARIA,
                FuncionarioTipo.BALNEARIO, FuncionarioTipo.BALNEARIO,
                FuncionarioTipo.ESCOLA,
                FuncionarioTipo.INTERNO, FuncionarioTipo.INTERNO
        };

        for (int i = 0; i < funcoes.length; i++) {
            FuncionarioRegisterRequest request = new FuncionarioRegisterRequest(
                    "Func Novo",
                    "novo" + i + "@func.com",
                    "pass123",
                    "123456789",
                    "912345678",
                    funcoes[i],
                    LocalDate.now(),
                    true);

            when(utilizadorRepository.existsByEmail("novo" + i + "@func.com")).thenReturn(false);
            when(cryptoUtils.generateBlindIndex("123456789")).thenReturn("blind_index");
            when(utilizadorRepository.existsByNifHash("blind_index")).thenReturn(false);
            when(passwordEncoder.encode("pass123")).thenReturn("hashed_pass");

            final FuncionarioTipo expectedTipo = tiposEsperados[i];
            when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(invocation -> {
                Funcionario saved = invocation.getArgument(0);
                saved.setId((long) (40 + expectedTipo.ordinal()));
                return saved;
            });

            AuthService.AuthResult result = authService.registerFuncionario(request);

            assertNotNull(result);
            assertEquals(expectedTipo.name(), result.response().role());
            assertFalse(result.response().active());
        }
    }

    @Test
    void registerFuncionario_DeveLancarErroQuandoTermosNaoAceites() {
        FuncionarioRegisterRequest request = new FuncionarioRegisterRequest(
                "Func Novo",
                "novo@func.com",
                "pass123",
                "123456789",
                "912345678",
                "SECRETARIA",
                LocalDate.now(),
                false);

        assertThrows(BadRequestException.class, () -> authService.registerFuncionario(request));
    }

    @Test
    void registerFuncionario_DeveLancarErroQuandoFuncaoNull() {
        FuncionarioRegisterRequest request = new FuncionarioRegisterRequest(
                "Func Novo",
                "novo@func.com",
                "pass123",
                "123456789",
                "912345678",
                null,
                LocalDate.now(),
                true);

        when(utilizadorRepository.existsByEmail("novo@func.com")).thenReturn(false);
        when(cryptoUtils.generateBlindIndex("123456789")).thenReturn("blind_index");
        when(utilizadorRepository.existsByNifHash("blind_index")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.registerFuncionario(request));
    }

    @Test
    void registerFuncionario_DeveLancarErroQuandoFuncaoDesconhecida() {
        FuncionarioRegisterRequest request = new FuncionarioRegisterRequest(
                "Func Novo",
                "novo@func.com",
                "pass123",
                "123456789",
                "912345678",
                "CEO_DESCONHECIDO",
                LocalDate.now(),
                true);

        when(utilizadorRepository.existsByEmail("novo@func.com")).thenReturn(false);
        when(cryptoUtils.generateBlindIndex("123456789")).thenReturn("blind_index");
        when(utilizadorRepository.existsByNifHash("blind_index")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.registerFuncionario(request));
    }

    // ==========================================
    // UPDATE PASSWORD TESTS
    // ==========================================

    @Test
    void updatePassword_DeveLancarErroQuandoUtilizadorNaoExiste() {
        when(utilizadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.updatePassword(99L, "nova", true));
    }

    @Test
    void updatePassword_DeveLancarErroQuandoExigeTermosMasNaoAceitou() {
        Utente utente = new Utente();
        utente.setTermsAcceptedAt(null);

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(utente));

        assertThrows(BadRequestException.class, () -> authService.updatePassword(1L, "nova", false));
        assertThrows(BadRequestException.class, () -> authService.updatePassword(1L, "nova", null));
    }

    @Test
    void updatePassword_Utente_Sucesso_AceitaTermosAgora() {
        Utente utente = new Utente();
        utente.setId(1L);
        utente.setTermsAcceptedAt(null);

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(passwordEncoder.encode("nova")).thenReturn("hashed");

        authService.updatePassword(1L, "nova", true);

        assertTrue(utente.isActivo());
        assertNotNull(utente.getTermsAcceptedAt());
        assertEquals("hashed", utente.getPassword());
        verify(utenteRepository).save(utente);
    }

    @Test
    void updatePassword_Funcionario_Sucesso_AceitouTermosNoPassado() {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(2L);
        funcionario.setTermsAcceptedAt(LocalDateTime.now().minusDays(1));
        funcionario.setActivo(false);

        when(utilizadorRepository.findById(2L)).thenReturn(Optional.of(funcionario));
        when(passwordEncoder.encode("nova")).thenReturn("hashed");

        authService.updatePassword(2L, "nova", null);

        assertTrue(funcionario.isActivo());
        verify(funcionarioRepository).save(funcionario);
    }

    @Test
    void updatePassword_UtilizadorGenerico_Sucesso() {
        Utilizador utilizador = new Utilizador() {
        };
        utilizador.setId(3L);
        utilizador.setTermsAcceptedAt(LocalDateTime.now());

        when(utilizadorRepository.findById(3L)).thenReturn(Optional.of(utilizador));
        when(passwordEncoder.encode("nova")).thenReturn("hashed");

        authService.updatePassword(3L, "nova", null);

        assertEquals("hashed", utilizador.getPassword());
        verify(utilizadorRepository).save(utilizador);
    }

    // ==========================================
    // GET CURRENT USER RESPONSE TESTS
    // ==========================================

    @Test
    void getCurrentUserResponse_NullPrincipal_DeveRetornarVazio() {
        var res = authService.getCurrentUserResponse(null);
        assertFalse(res.isPresent());
    }

    @Test
    void getCurrentUserResponse_NaoEncontradoNoRepository_DeveRetornarVazio() {
        Utente utente = new Utente();
        utente.setId(50L);

        when(utilizadorRepository.findById(50L)).thenReturn(Optional.empty());

        var res = authService.getCurrentUserResponse(utente);
        assertFalse(res.isPresent());
    }

    @Test
    void getCurrentUserResponse_Sucesso_ComRolesDinamicamenteMapeadas() {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(60L);
        funcionario.setTermsAcceptedAt(LocalDateTime.now());
        funcionario.setActivo(true);
        funcionario.setTipo(FuncionarioTipo.BALNEARIO);

        when(utilizadorRepository.findById(60L)).thenReturn(Optional.of(funcionario));

        var res = authService.getCurrentUserResponse(funcionario);

        assertTrue(res.isPresent());
        assertEquals("BALNEARIO", res.get().role());
        assertTrue(res.get().active());
    }

    // ==========================================
    // GET CURRENT USER ID & ADMIN TESTS
    // ==========================================

    @Test
    void getCurrentUserId_NaoAutenticadoOuAnonymous() {
        SecurityContextHolder.clearContext();
        assertNull(authService.getCurrentUserId());

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertNull(authService.getCurrentUserId());

        Authentication anonymousAuth = mock(AnonymousAuthenticationToken.class);
        when(anonymousAuth.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(anonymousAuth);

        assertNull(authService.getCurrentUserId());
    }

    @Test
    void getCurrentUserId_ComUtilizadorPrincipal() {
        Utente utente = new Utente();
        utente.setId(70L);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(utente);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertEquals(70L, authService.getCurrentUserId());
    }

    @Test
    void getCurrentUserId_ComUserDetailsPrincipal() {
        UserDetails userDetails = User.withUsername("util@teste.com")
                .password("pass")
                .authorities(Collections.emptyList())
                .build();

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Cenário 1: Encontra utilizador no repositório
        Utente utente = new Utente();
        utente.setId(80L);
        when(utilizadorRepository.findByEmail("util@teste.com")).thenReturn(List.of(utente));

        assertEquals(80L, authService.getCurrentUserId());

        // Cenário 2: Não encontra utilizador no repositório
        when(utilizadorRepository.findByEmail("util@teste.com")).thenReturn(Collections.emptyList());

        assertNull(authService.getCurrentUserId());
    }

    @Test
    void getCurrentUserId_ComPrincipalDesconhecido() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("Desconhecido");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertNull(authService.getCurrentUserId());
    }

    @Test
    void isAdmin_Testes() {
        // Cenário 1: Nulo ou Não autenticado
        assertFalse(authService.isAdmin());

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertFalse(authService.isAdmin());

        // Cenário 2: Autenticado mas sem ROLE_SECRETARIA
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_BALNEARIO"))).when(authentication).getAuthorities();

        assertFalse(authService.isAdmin());

        // Cenário 3: Autenticado com ROLE_SECRETARIA
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_SECRETARIA"))).when(authentication).getAuthorities();

        assertTrue(authService.isAdmin());
    }
}