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
}
