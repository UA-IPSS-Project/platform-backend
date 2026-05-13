package pt.florinhas.api_gateway.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import pt.florinhas.api_gateway.dto.LoginFuncionarioRequest;
import pt.florinhas.api_gateway.dto.UtenteRegisterRequest;
import pt.florinhas.common_data.domain.Utente;
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

        ReflectionTestUtils.setField(
                authService,
                "jwtExpiration",
                86400000L);
    }
    @Test
    void registerUtente_DeveRegistarUtente() {

        UtenteRegisterRequest request =
                new UtenteRegisterRequest(
                        "Teste",
                        "teste@teste.com",
                        "123456789",
                        "912345678",
                        "password",
                        LocalDate.now(),
                        true
                );

        when(utilizadorRepository.existsByEmail(any()))
                .thenReturn(false);

        when(cryptoUtils.generateBlindIndex(any()))
                .thenReturn("hash");

        when(utilizadorRepository.existsByNifHash(any()))
                .thenReturn(false);

        when(passwordEncoder.encode(any()))
                .thenReturn("encoded");

        when(utenteRepository.save(any()))
                .thenAnswer(i -> {
                    Utente u = i.getArgument(0);
                    u.setId(1L);
                    return u;
                });

        AuthService.AuthResult result =
                authService.registerUtente(request);

        assertNotNull(result);
        assertEquals("UTENTE", result.response().role());

        verify(utenteRepository).save(any());
    }
     @Test
    void getCurrentUserResponse_DeveRetornarResponse() {

        Utente utente = new Utente();
        utente.setId(1L);
        utente.setNome("Teste");
        utente.setEmail("teste@teste.com");
        utente.setActivo(true);

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(utente));

        var response =
                authService.getCurrentUserResponse(utente);

        assertTrue(response.isPresent());
        assertEquals("Teste", response.get().nome());
    }

    @Test
    void requiresPasswordSetup_DeveRetornarTrue() {

        Utente utente = new Utente();
        utente.setTermsAcceptedAt(null);

        boolean result =
                authService.requiresPasswordSetup(utente, false);

        assertTrue(result);
    }
}