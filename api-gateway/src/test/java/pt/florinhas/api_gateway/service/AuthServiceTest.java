package pt.florinhas.api_gateway.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import pt.florinhas.api_gateway.dto.AuthResponse;
import pt.florinhas.api_gateway.dto.LoginFuncionarioRequest;
import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.exception.BadRequestException;
import pt.florinhas.common_data.repository.FuncionarioRepository;
import pt.florinhas.common_data.repository.UtenteRepository;
import pt.florinhas.common_data.repository.UtilizadorRepository;
import pt.florinhas.common_data.security.CryptoUtils;
import pt.florinhas.common_data.validation.NifValidator;

class AuthServiceTest {

    private UtilizadorRepository utilizadorRepository;
    private FuncionarioRepository funcionarioRepository;
    private UtenteRepository utenteRepository;
    private PasswordEncoder passwordEncoder;
    private NifValidator nifValidator;
    private CryptoUtils cryptoUtils;

    private AuthService service;

    @BeforeEach
    void setUp() throws Exception {

        utilizadorRepository = mock(UtilizadorRepository.class);
        funcionarioRepository = mock(FuncionarioRepository.class);
        utenteRepository = mock(UtenteRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        nifValidator = mock(NifValidator.class);
        cryptoUtils = mock(CryptoUtils.class);

        WebClient.Builder builder = mock(WebClient.Builder.class);
        WebClient webClient = mock(WebClient.class);

        when(builder.baseUrl(any()))
                .thenReturn(builder);

        when(builder.defaultHeader(any(), any()))
                .thenReturn(builder);

        when(builder.build())
                .thenReturn(webClient);

        service = new AuthService(
                utilizadorRepository,
                funcionarioRepository,
                utenteRepository,
                passwordEncoder,
                nifValidator,
                cryptoUtils,
                builder,
                "http://localhost",
                "secret");

        setField("jwtExpiration", 86400000L);
    }

    @Test
    void loginFuncionario_DeveAutenticar() {

        Funcionario funcionario = new Funcionario();

        funcionario.setId(1L);
        funcionario.setNome("Teste");
        funcionario.setEmail("teste@teste.com");
        funcionario.setPassHash("hash");
        funcionario.setTipo(FuncionarioTipo.SECRETARIA);
        funcionario.setActivo(true);

        when(utilizadorRepository.findByEmail("teste@teste.com"))
                .thenReturn(List.of(funcionario));

        when(passwordEncoder.matches("123", "hash"))
                .thenReturn(true);

        var result = service.loginFuncionario(
                new LoginFuncionarioRequest(
                        "teste@teste.com",
                        "123"));

        assertEquals(
                "SECRETARIA",
                result.response().role());
    }

    @Test
    void loginFuncionario_DeveFalharQuandoPasswordInvalida() {

        Funcionario funcionario = new Funcionario();

        funcionario.setPassHash("hash");

        when(utilizadorRepository.findByEmail("teste@teste.com"))
                .thenReturn(List.of(funcionario));

        when(passwordEncoder.matches("123", "hash"))
                .thenReturn(false);

        assertThrows(
                BadRequestException.class,
                () -> service.loginFuncionario(
                        new LoginFuncionarioRequest(
                                "teste@teste.com",
                                "123")));
    }

    @Test
    void requiresPasswordSetup_DeveRetornarTrue() {

        Funcionario funcionario = new Funcionario();

        funcionario.setTermsAcceptedAt(null);

        boolean result =
                service.requiresPasswordSetup(
                        funcionario,
                        false);

        assertTrue(result);
    }

    @Test
    void requiresPasswordSetup_DeveRetornarFalse() {

        Funcionario funcionario = new Funcionario();

        funcionario.setTermsAcceptedAt(
                LocalDateTime.now());

        boolean result =
                service.requiresPasswordSetup(
                        funcionario,
                        true);

        assertFalse(result);
    }

    @Test
    void generateAuthResponse_DeveCriarResponse() {

        Funcionario funcionario = new Funcionario();

        funcionario.setId(1L);
        funcionario.setNome("Teste");
        funcionario.setEmail("teste@teste.com");
        funcionario.setNif("123456789");
        funcionario.setTelefone("912345678");

        var result =
                service.generateAuthResponse(
                        funcionario,
                        "SECRETARIA",
                        true);

        AuthResponse response =
                result.response();

        assertEquals(
                "Teste",
                response.nome());

        assertEquals(
                "SECRETARIA",
                response.role());
    }

    @Test
    void getCurrentUserResponse_DeveRetornarEmptyQuandoNull() {

        Optional<AuthResponse> result =
                service.getCurrentUserResponse(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentUserResponse_DeveRetornarUtilizador() {

        Funcionario funcionario = new Funcionario();

        funcionario.setId(1L);
        funcionario.setNome("Teste");
        funcionario.setEmail("teste@teste.com");
        funcionario.setTipo(FuncionarioTipo.SECRETARIA);
        funcionario.setActivo(true);

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(funcionario));

        Optional<AuthResponse> result =
                service.getCurrentUserResponse(funcionario);

        assertTrue(result.isPresent());

        assertEquals(
                "SECRETARIA",
                result.get().role());
    }

    @Test
    void updatePassword_DeveFalharSemAceitarTermos() {

        Funcionario funcionario = new Funcionario();

        funcionario.setTermsAcceptedAt(null);

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(funcionario));

        assertThrows(
                BadRequestException.class,
                () -> service.updatePassword(
                        1L,
                        "novaPassword",
                        false));
    }

    @Test
    void updatePassword_DeveAtualizarPassword() {

        Funcionario funcionario = new Funcionario();

        funcionario.setTermsAcceptedAt(
                LocalDateTime.now());

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.of(funcionario));

        when(passwordEncoder.encode("novaPassword"))
                .thenReturn("hash");

        assertDoesNotThrow(
                () -> service.updatePassword(
                        1L,
                        "novaPassword",
                        true));
    }

    private void setField(
            String fieldName,
            Object value
    ) throws Exception {

        Field field =
                AuthService.class
                        .getDeclaredField(fieldName);

        field.setAccessible(true);

        field.set(service, value);
    }

    @Test
        void loginFuncionario_DeveFalharQuandoUtilizadorNaoEncontrado() {

        when(utilizadorRepository.findByEmail(
                "naoexiste@teste.com"))
                .thenReturn(List.of());

        assertThrows(
                BadRequestException.class,
                () -> service.loginFuncionario(
                        new LoginFuncionarioRequest(
                                "naoexiste@teste.com",
                                "123")));
        }

        @Test
        void loginFuncionario_DeveFalharQuandoFuncionarioInativoEAprovado() {

        Funcionario funcionario = new Funcionario();

        funcionario.setId(1L);
        funcionario.setNome("Teste");
        funcionario.setEmail("teste@teste.com");
        funcionario.setPassHash("hash");
        funcionario.setTipo(FuncionarioTipo.SECRETARIA);

        funcionario.setActivo(false);

        funcionario.setTermsAcceptedAt(
                LocalDateTime.now());

        when(utilizadorRepository.findByEmail(
                "teste@teste.com"))
                .thenReturn(List.of(funcionario));

        when(passwordEncoder.matches(
                "123",
                "hash"))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> service.loginFuncionario(
                        new LoginFuncionarioRequest(
                                "teste@teste.com",
                                "123")));
        }

        @Test
        void getCurrentUserResponse_DeveRetornarEmptyQuandoUtilizadorNaoExiste() {

        Funcionario funcionario =
                new Funcionario();

        funcionario.setId(999L);

        when(utilizadorRepository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<AuthResponse> result =
                service.getCurrentUserResponse(
                        funcionario);

        assertTrue(
                result.isEmpty());
        }

        @Test
        void updatePassword_DeveFalharQuandoUtilizadorNaoExiste() {

        when(utilizadorRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                BadRequestException.class,
                () -> service.updatePassword(
                        1L,
                        "novaPassword",
                        true));
        }
}