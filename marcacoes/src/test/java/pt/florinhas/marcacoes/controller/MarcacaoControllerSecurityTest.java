package pt.florinhas.marcacoes.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import pt.florinhas.marcacoes.config.TestSecurityConfig;
import pt.florinhas.marcacoes.service.AuthService;
import pt.florinhas.marcacoes.service.MarcacaoService;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.UtilizadorRepository;
import pt.florinhas.marcacoes.dto.MarcacaoResponseDTO;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MarcacaoController.class, excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
@Import(TestSecurityConfig.class)
public class MarcacaoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MarcacaoService marcacaoService;

    @MockitoBean
    private AuthService authService;

    // Repositories needed for context
    @MockitoBean
    private UtenteRepository utenteRepository;
    @MockitoBean
    private FuncionarioRepository funcionarioRepository;
    @MockitoBean
    private UtilizadorRepository utilizadorRepository;
    @MockitoBean
    private pt.florinhas.marcacoes.security.JwtService jwtService;
    @MockitoBean(name = "customUserDetailsService")
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockitoBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @MockitoBean
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @Test
    @WithMockUser(username = "user", roles = { "UTENTE" })
    void consultarMarcacoesUtente_AsUser_AccessingOtherUser_ShouldBeForbidden() throws Exception {
        // Arrange
        Long currentUserId = 2L;
        Long targetUserId = 5L; // Other user

        // Mock AuthService to return current user
        when(authService.getCurrentUserId()).thenReturn(currentUserId);
        when(authService.isAdmin()).thenReturn(false);

        // Service would return empty list
        when(marcacaoService.consultarMarcacoesUtente(targetUserId)).thenReturn(Collections.emptyList());

        // Act & Assert
        // This is expected to FAIL (return 200 OK) before the fix
        // We want it to be 403 Forbidden
        mockMvc.perform(get("/api/marcacoes/utente/{utenteId}", targetUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = { "UTENTE" })
    void atualizarEstadoMarcacao_AsUser_UpdatingOthersMarcacao_ShouldBeForbidden() throws Exception {
        // Arrange
        Long currentUserId = 2L;
        Long marcacaoId = 99L;

        // Mock finding the marcacao to belong to another user
        MarcacaoResponseDTO mockDto = new MarcacaoResponseDTO();
        MarcacaoResponseDTO.MarcacaoSecretariaDTO secDto = new MarcacaoResponseDTO.MarcacaoSecretariaDTO();
        MarcacaoResponseDTO.UtenteDTO utenteDto = new MarcacaoResponseDTO.UtenteDTO();
        utenteDto.setId(5L); // Owner is ID 5 (Different from currentUserId 2)
        secDto.setUtente(utenteDto);
        mockDto.setMarcacaoSecretaria(secDto);

        when(authService.getCurrentUserId()).thenReturn(currentUserId);
        when(authService.isAdmin()).thenReturn(false);

        // Mock the retrieval which is now used for security check
        when(marcacaoService.obterMarcacaoDTO(marcacaoId)).thenReturn(mockDto);

        // We don't expect the update method to be called if security check fails
        // But if it were called, it would return empty

        mockMvc.perform(put("/api/marcacoes/{id}/estado", marcacaoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"novoEstado\": \"CANCELADO\"}")) // JSON body
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = { "UTENTE" })
    void procurarAgenda_AsUser_SearchingOtherUser_ShouldBeForbidden() throws Exception {
        // Arrange
        Long currentUserId = 2L;
        Long targetUserId = 5L; // Other user

        when(authService.getCurrentUserId()).thenReturn(currentUserId);
        when(authService.isAdmin()).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/agenda/procurar")
                .param("utenteId", targetUserId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ==========================================================
    // Testes para correção IDOR - Endpoint /funcionario/{id}
    // ==========================================================

    @Test
    @WithMockUser(username = "admin", roles = { "FUNCIONARIO" })
    void consultarMarcacoesFuncionario_AsAdmin_ShouldAllowAnyId() throws Exception {
        // Arrange
        Long funcionarioId = 99L;
        when(authService.getCurrentUserId()).thenReturn(1L);
        when(authService.isAdmin()).thenReturn(true);
        when(marcacaoService.consultarMarcacoesFuncionario(funcionarioId))
                .thenReturn(java.util.Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/funcionario/{funcionarioId}", funcionarioId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "funcionario", roles = { "FUNCIONARIO" })
    void consultarMarcacoesFuncionario_AsFuncionario_AccessingOwnId_ShouldSucceed() throws Exception {
        // Arrange
        Long funcionarioId = 5L;
        when(authService.getCurrentUserId()).thenReturn(funcionarioId);
        when(authService.isAdmin()).thenReturn(true); // Funcionários são admins
        when(marcacaoService.consultarMarcacoesFuncionario(funcionarioId))
                .thenReturn(java.util.Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/funcionario/{funcionarioId}", funcionarioId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "utente", roles = { "UTENTE" })
    void consultarMarcacoesFuncionario_AsUtente_AccessingOtherId_ShouldBeForbidden() throws Exception {
        // Arrange
        Long currentUserId = 2L;
        Long funcionarioId = 5L; // Not same as currentUserId

        when(authService.getCurrentUserId()).thenReturn(currentUserId);
        when(authService.isAdmin()).thenReturn(false);

        // Act & Assert - IDOR protection should block this
        mockMvc.perform(get("/api/marcacoes/funcionario/{funcionarioId}", funcionarioId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ==========================================================
    // Testes para correção IDOR - Endpoint /{id}
    // ==========================================================

    @Test
    @WithMockUser(username = "admin", roles = { "FUNCIONARIO" })
    void obterMarcacao_AsAdmin_ShouldAllowAnyId() throws Exception {
        // Arrange
        Long marcacaoId = 99L;
        MarcacaoResponseDTO mockDto = new MarcacaoResponseDTO();
        mockDto.setId(marcacaoId);

        when(authService.getCurrentUserId()).thenReturn(1L);
        when(authService.isAdmin()).thenReturn(true);
        when(marcacaoService.obterMarcacaoDTO(marcacaoId)).thenReturn(mockDto);

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/{id}", marcacaoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99));
    }

    @Test
    @WithMockUser(username = "utente", roles = { "UTENTE" })
    void obterMarcacao_AsUtente_AccessingOwnMarcacao_ShouldSucceed() throws Exception {
        // Arrange
        Long currentUserId = 2L;
        Long marcacaoId = 10L;

        MarcacaoResponseDTO mockDto = new MarcacaoResponseDTO();
        mockDto.setId(marcacaoId);
        MarcacaoResponseDTO.MarcacaoSecretariaDTO secDto = new MarcacaoResponseDTO.MarcacaoSecretariaDTO();
        MarcacaoResponseDTO.UtenteDTO utenteDto = new MarcacaoResponseDTO.UtenteDTO();
        utenteDto.setId(currentUserId); // Marcação pertence ao utilizador atual
        secDto.setUtente(utenteDto);
        mockDto.setMarcacaoSecretaria(secDto);

        when(authService.getCurrentUserId()).thenReturn(currentUserId);
        when(authService.isAdmin()).thenReturn(false);
        when(marcacaoService.obterMarcacaoDTO(marcacaoId)).thenReturn(mockDto);

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/{id}", marcacaoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser(username = "utente", roles = { "UTENTE" })
    void obterMarcacao_AsUtente_AccessingOthersMarcacao_ShouldBeForbidden() throws Exception {
        // Arrange
        Long currentUserId = 2L;
        Long marcacaoId = 10L;

        MarcacaoResponseDTO mockDto = new MarcacaoResponseDTO();
        mockDto.setId(marcacaoId);
        MarcacaoResponseDTO.MarcacaoSecretariaDTO secDto = new MarcacaoResponseDTO.MarcacaoSecretariaDTO();
        MarcacaoResponseDTO.UtenteDTO utenteDto = new MarcacaoResponseDTO.UtenteDTO();
        utenteDto.setId(99L); // Marcação pertence a OUTRO utilizador
        secDto.setUtente(utenteDto);
        mockDto.setMarcacaoSecretaria(secDto);

        when(authService.getCurrentUserId()).thenReturn(currentUserId);
        when(authService.isAdmin()).thenReturn(false);
        when(marcacaoService.obterMarcacaoDTO(marcacaoId)).thenReturn(mockDto);

        // Act & Assert - IDOR protection should block this
        mockMvc.perform(get("/api/marcacoes/{id}", marcacaoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "utente", roles = { "UTENTE" })
    void obterMarcacao_NotFound_ShouldReturn404() throws Exception {
        // Arrange
        Long marcacaoId = 999L;

        when(authService.getCurrentUserId()).thenReturn(2L);
        when(authService.isAdmin()).thenReturn(false);
        when(marcacaoService.obterMarcacaoDTO(marcacaoId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/{id}", marcacaoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
