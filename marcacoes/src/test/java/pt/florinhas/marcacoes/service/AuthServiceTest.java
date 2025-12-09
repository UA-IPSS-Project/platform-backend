// package pt.florinhas.marcacoes.service;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import pt.florinhas.marcacoes.domain.Funcionario;
// import pt.florinhas.marcacoes.domain.FuncionarioTipo;
// import pt.florinhas.marcacoes.domain.Utente;
// import pt.florinhas.marcacoes.dto.AuthResponse;
// import pt.florinhas.marcacoes.dto.LoginRequest;
// import pt.florinhas.marcacoes.dto.RegisterRequest;
// import pt.florinhas.marcacoes.exception.BadRequestException;
// import pt.florinhas.marcacoes.repository.FuncionarioRepository;
// import pt.florinhas.marcacoes.repository.UtenteRepository;
// import pt.florinhas.marcacoes.repository.UtilizadorRepository;
// import pt.florinhas.marcacoes.security.JwtService;

// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class AuthServiceTest {

//     @Mock
//     private UtilizadorRepository utilizadorRepository;

//     @Mock
//     private FuncionarioRepository funcionarioRepository;

//     @Mock
//     private UtenteRepository utenteRepository;

//     @Mock
//     private PasswordEncoder passwordEncoder;

//     @Mock
//     private JwtService jwtService;

//     @Mock
//     private AuthenticationManager authenticationManager;

//     @InjectMocks
//     private AuthService authService;

//     private LoginRequest loginRequest;
//     private RegisterRequest registerRequestUtente;
//     private RegisterRequest registerRequestFuncionario;
//     private Utente utente;
//     private Funcionario funcionario;

//     @BeforeEach
//     void setUp() {
//         loginRequest = new LoginRequest("test@example.com", "password123");

//         registerRequestUtente = new RegisterRequest(
//                 "Test User",
//                 "test@example.com",
//                 "password123",
//                 "123456789",
//                 "912345678",
//                 "UTENTE"
//         );

//         registerRequestFuncionario = new RegisterRequest(
//                 "Test Funcionario",
//                 "func@example.com",
//                 "password123",
//                 "987654321",
//                 "912345679",
//                 "FUNCIONARIO"
//         );

//         utente = new Utente();
//         utente.setId(1L);
//         utente.setEmail("test@example.com");
//         utente.setNome("Test User");
//         utente.setPassHash("hashedPassword");

//         funcionario = new Funcionario();
//         funcionario.setId(2L);
//         funcionario.setEmail("func@example.com");
//         funcionario.setNome("Test Funcionario");
//         funcionario.setPassHash("hashedPassword");
//         funcionario.setTipo(FuncionarioTipo.SECRETARIA);
//     }

//     @Test
//     void login_DeveRetornarAuthResponse_QuandoCredenciaisValidas() {
//         // Arrange
//         when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                 .thenReturn(null);
//         when(utilizadorRepository.findByEmail("test@example.com"))
//                 .thenReturn(Optional.of(utente));
//         when(jwtService.generateToken(any()))
//                 .thenReturn("jwt-token");

//         // Act
//         AuthResponse response = authService.login(loginRequest);

//         // Assert
//         assertNotNull(response);
//         assertEquals("jwt-token", response.token());
//         assertEquals("test@example.com", response.email());
//         assertEquals("Test User", response.nome());
//         assertEquals("UTENTE", response.role());
//         verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
//         verify(utilizadorRepository).findByEmail("test@example.com");
//         verify(jwtService).generateToken(utente);
//     }

//     @Test
//     void login_DeveLancarException_QuandoUtilizadorNaoExiste() {
//         // Arrange
//         when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                 .thenReturn(null);
//         when(utilizadorRepository.findByEmail(anyString()))
//                 .thenReturn(Optional.empty());

//         // Act & Assert
//         assertThrows(BadRequestException.class, () -> authService.login(loginRequest));
//         verify(utilizadorRepository).findByEmail("test@example.com");
//     }

//     @Test
//     void register_DeveCriarUtente_QuandoDadosValidos() {
//         // Arrange
//         when(utilizadorRepository.existsByEmail(anyString())).thenReturn(false);
//         when(utilizadorRepository.existsByNif(anyString())).thenReturn(false);
//         when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
//         when(utenteRepository.save(any(Utente.class))).thenReturn(utente);
//         when(jwtService.generateToken(any())).thenReturn("jwt-token");

//         // Act
//         AuthResponse response = authService.register(registerRequestUtente);

//         // Assert
//         assertNotNull(response);
//         assertEquals("jwt-token", response.token());
//         assertEquals("UTENTE", response.role());
//         verify(utilizadorRepository).existsByEmail("test@example.com");
//         verify(utilizadorRepository).existsByNif("123456789");
//         verify(passwordEncoder).encode("password123");
//         verify(utenteRepository).save(any(Utente.class));
//         verify(jwtService).generateToken(any());
//     }

//     @Test
//     void register_DeveCriarFuncionario_QuandoRoleFuncionario() {
//         // Arrange
//         when(utilizadorRepository.existsByEmail(anyString())).thenReturn(false);
//         when(utilizadorRepository.existsByNif(anyString())).thenReturn(false);
//         when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
//         when(funcionarioRepository.save(any(Funcionario.class))).thenReturn(funcionario);
//         when(jwtService.generateToken(any())).thenReturn("jwt-token");

//         // Act
//         AuthResponse response = authService.register(registerRequestFuncionario);

//         // Assert
//         assertNotNull(response);
//         assertEquals("jwt-token", response.token());
//         assertEquals("FUNCIONARIO", response.role());
//         verify(funcionarioRepository).save(any(Funcionario.class));
//     }

//     @Test
//     void register_DeveLancarException_QuandoEmailJaExiste() {
//         // Arrange
//         when(utilizadorRepository.existsByEmail("test@example.com")).thenReturn(true);

//         // Act & Assert
//         BadRequestException exception = assertThrows(
//                 BadRequestException.class,
//                 () -> authService.register(registerRequestUtente)
//         );
        
//         assertTrue(exception.getMessage().contains("Email já está em uso"));
//         verify(utilizadorRepository).existsByEmail("test@example.com");
//         verify(utenteRepository, never()).save(any());
//     }

//     @Test
//     void register_DeveLancarException_QuandoNifJaExiste() {
//         // Arrange
//         when(utilizadorRepository.existsByEmail(anyString())).thenReturn(false);
//         when(utilizadorRepository.existsByNif("123456789")).thenReturn(true);

//         // Act & Assert
//         BadRequestException exception = assertThrows(
//                 BadRequestException.class,
//                 () -> authService.register(registerRequestUtente)
//         );
        
//         assertTrue(exception.getMessage().contains("NIF já está em uso"));
//         verify(utilizadorRepository).existsByNif("123456789");
//         verify(utenteRepository, never()).save(any());
//     }

//     @Test
//     void register_DeveLancarException_QuandoRoleInvalido() {
//         // Arrange
//         RegisterRequest invalidRequest = new RegisterRequest(
//                 "Test",
//                 "test@example.com",
//                 "password123",
//                 "123456789",
//                 "912345678",
//                 "INVALID_ROLE"
//         );
//         when(utilizadorRepository.existsByEmail(anyString())).thenReturn(false);
//         when(utilizadorRepository.existsByNif(anyString())).thenReturn(false);

//         // Act & Assert
//         BadRequestException exception = assertThrows(
//                 BadRequestException.class,
//                 () -> authService.register(invalidRequest)
//         );
        
//         assertTrue(exception.getMessage().contains("Role inválido"));
//         verify(utenteRepository, never()).save(any());
//         verify(funcionarioRepository, never()).save(any());
//     }
// }
