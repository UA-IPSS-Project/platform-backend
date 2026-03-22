// package pt.florinhas.marcacoes.controller;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;
// import pt.florinhas.marcacoes.dto.AuthResponse;
// import pt.florinhas.marcacoes.dto.LoginRequest;
// import pt.florinhas.marcacoes.dto.RegisterRequest;
// import pt.florinhas.marcacoes.exception.BadRequestException;
// import pt.florinhas.marcacoes.service.AuthService;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @SpringBootTest
// @AutoConfigureMockMvc
// class AuthControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @MockBean
//     private AuthService authService;

//     private LoginRequest loginRequest;
//     private RegisterRequest registerRequest;
//     private AuthResponse authResponse;

//     @BeforeEach
//     void setUp() {
//         loginRequest = new LoginRequest("test@example.com", "password123");

//         registerRequest = new RegisterRequest(
//                 "Test User",
//                 "test@example.com",
//                 "password123",
//                 "123456789",
//                 "912345678",
//                 "UTENTE"
//         );

//         authResponse = new AuthResponse(
//                 1L,
//                 "test@example.com",
//                 "Test User",
//                 "UTENTE"
//         );
//     }

//     @Test
//     void login_DeveRetornar200_QuandoCredenciaisValidas() throws Exception {
//         // Arrange
//         //when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

//         // Act & Assert
//         mockMvc.perform(post("/api/auth/login")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(loginRequest)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.token").exists())
//                 .andExpect(jsonPath("$.email").value("test@example.com"))
//                 .andExpect(jsonPath("$.nome").value("Test User"))
//                 .andExpect(jsonPath("$.role").value("UTENTE"));
//     }

//     @Test
//     void login_DeveRetornar400_QuandoEmailVazio() throws Exception {
//         // Arrange
//         LoginRequest invalidRequest = new LoginRequest("", "password123");

//         // Act & Assert
//         mockMvc.perform(post("/api/auth/login")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(invalidRequest)))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void login_DeveRetornar400_QuandoPasswordVazia() throws Exception {
//         // Arrange
//         LoginRequest invalidRequest = new LoginRequest("test@example.com", "");

//         // Act & Assert
//         mockMvc.perform(post("/api/auth/login")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(invalidRequest)))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void login_DeveRetornar400_QuandoEmailInvalido() throws Exception {
//         // Arrange
//         LoginRequest invalidRequest = new LoginRequest("invalid-email", "password123");

//         // Act & Assert
//         mockMvc.perform(post("/api/auth/login")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(invalidRequest)))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void register_DeveRetornar200_QuandoDadosValidos() throws Exception {
//         // Arrange
//         when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

//         // Act & Assert
//         mockMvc.perform(post("/api/auth/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(registerRequest)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.token").exists())
//                 .andExpect(jsonPath("$.email").value("test@example.com"))
//                 .andExpect(jsonPath("$.nome").value("Test User"))
//                 .andExpect(jsonPath("$.role").value("UTENTE"));
//     }

//     @Test
//     void register_DeveRetornar400_QuandoNomeVazio() throws Exception {
//         // Arrange
//         RegisterRequest invalidRequest = new RegisterRequest(
//                 "",
//                 "test@example.com",
//                 "password123",
//                 "123456789",
//                 "912345678",
//                 "UTENTE"
//         );

//         // Act & Assert
//         mockMvc.perform(post("/api/auth/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(invalidRequest)))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void register_DeveRetornar400_QuandoPasswordCurta() throws Exception {
//         // Arrange
//         RegisterRequest invalidRequest = new RegisterRequest(
//                 "Test User",
//                 "test@example.com",
//                 "12345",
//                 "123456789",
//                 "912345678",
//                 "UTENTE"
//         );

//         // Act & Assert
//         mockMvc.perform(post("/api/auth/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(invalidRequest)))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void register_DeveRetornar400_QuandoNifInvalido() throws Exception {
//         // Arrange
//         RegisterRequest invalidRequest = new RegisterRequest(
//                 "Test User",
//                 "test@example.com",
//                 "password123",
//                 "12345", // NIF inválido
//                 "912345678",
//                 "UTENTE"
//         );

//         // Act & Assert
//         mockMvc.perform(post("/api/auth/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(invalidRequest)))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void register_DeveRetornar400_QuandoEmailJaExiste() throws Exception {
//         // Arrange
//         when(authService.register(any(RegisterRequest.class)))
//                 .thenThrow(new BadRequestException("Email já está em uso"));

//         // Act & Assert
//         mockMvc.perform(post("/api/auth/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(registerRequest)))
//                 .andExpect(status().isBadRequest());
//     }
// }
