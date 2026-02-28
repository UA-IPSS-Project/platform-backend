package pt.florinhas.marcacoes.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import pt.florinhas.marcacoes.domain.*;
import pt.florinhas.marcacoes.dto.*;
import pt.florinhas.marcacoes.repository.*;
import pt.florinhas.marcacoes.security.JwtService;
import pt.florinhas.marcacoes.service.AuthService;
import pt.florinhas.marcacoes.service.MarcacaoService;
import pt.florinhas.marcacoes.config.TestSecurityConfig;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MarcacaoController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import(TestSecurityConfig.class)
class MarcacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MarcacaoService marcacaoService;

    @MockitoBean
    private AuthService authService;

    // Repositories needed for Application Context even if not directly used in
    // Controller
    @MockitoBean
    private UtenteRepository utenteRepository;
    @MockitoBean
    private FuncionarioRepository funcionarioRepository;
    @MockitoBean
    private UtilizadorRepository utilizadorRepository;
    @MockitoBean
        private JwtService jwtService;
    @MockitoBean(name = "customUserDetailsService")
        private UserDetailsService userDetailsService;
    @MockitoBean
        private PasswordEncoder passwordEncoder;
    @MockitoBean
        private AuthenticationManager authenticationManager;

    private Funcionario funcionario;
    private Utente utente;
    private MarcacaoResponseDTO marcacaoDTO;

    @BeforeEach
    void setUp() {
        funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setNome("João Silva");

        utente = new Utente();
        utente.setId(2L);
        utente.setNome("Maria Santos");

        marcacaoDTO = new MarcacaoResponseDTO();
        marcacaoDTO.setId(1L);
        marcacaoDTO.setData(LocalDateTime.now().plusDays(1));
        marcacaoDTO.setEstado(EventoEstado.AGENDADO);
    }

    @Test
    @WithMockUser(username = "admin", roles = { "FUNCIONARIO" })
    void procurarAgenda_AsAdmin_ShouldAllowAnyUtenteId() throws Exception {
        // Arrange
        when(authService.getCurrentUserId()).thenReturn(1L);
        when(authService.isAdmin()).thenReturn(true);
        when(marcacaoService.procurarAgenda(any(), any(), any(), any(), any()))
                .thenReturn(List.of(marcacaoDTO));

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/agenda/procurar")
                .param("utenteId", "99") // ID arbitrário
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        // Verifica se chamou o serviço mantendo o utenteId=99
        verify(marcacaoService).procurarAgenda(any(), any(), any(), eq(99L), any());
    }

    @Test
    @WithMockUser(username = "user", roles = { "UTENTE" })
    void procurarAgenda_AsUser_ShouldAllowOwnId() throws Exception {
        // Arrange
        Long userId = 2L;
        when(authService.getCurrentUserId()).thenReturn(userId);
        when(authService.isAdmin()).thenReturn(false);
        when(marcacaoService.procurarAgenda(any(), any(), any(), any(), any()))
                .thenReturn(List.of(marcacaoDTO));

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/agenda/procurar")
                .param("utenteId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(marcacaoService).procurarAgenda(any(), any(), any(), eq(userId), any());
    }

    @Test
    @WithMockUser(username = "user", roles = { "UTENTE" })
    void procurarAgenda_AsUser_ShouldForbidden_OtherId() throws Exception {
        // Arrange
        Long userId = 2L;
        Long otherId = 3L;
        when(authService.getCurrentUserId()).thenReturn(userId);
        when(authService.isAdmin()).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/agenda/procurar")
                .param("utenteId", otherId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // 403

        verify(marcacaoService, never()).procurarAgenda(any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "user", roles = { "UTENTE" })
    void procurarAgenda_AsUser_NoId_ShouldDefaultToSelf() throws Exception {
        // Arrange
        Long userId = 2L;
        when(authService.getCurrentUserId()).thenReturn(userId);
        when(authService.isAdmin()).thenReturn(false);
        when(marcacaoService.procurarAgenda(any(), any(), any(), any(), any()))
                .thenReturn(List.of(marcacaoDTO));

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/agenda/procurar")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verifica se forçou utenteId = userId (2L)
        verify(marcacaoService).procurarAgenda(any(), any(), any(), eq(userId), any());
    }

    @Test
    @WithMockUser(username = "user", roles = { "UTENTE" })
    void consultarAgenda_AsUser_ShouldForbidden() throws Exception {
        // Arrange
        when(authService.isAdmin()).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/agenda"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "FUNCIONARIO" })
    void consultarAgenda_AsAdmin_ShouldOk() throws Exception {
        // Arrange
        when(authService.isAdmin()).thenReturn(true);
        when(marcacaoService.consultarAgenda(any(), any())).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/agenda"))
                .andExpect(status().isOk());
    }
}
