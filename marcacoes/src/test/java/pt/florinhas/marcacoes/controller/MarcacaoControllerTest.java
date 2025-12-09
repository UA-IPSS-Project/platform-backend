package pt.florinhas.marcacoes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import pt.florinhas.marcacoes.domain.*;
import pt.florinhas.marcacoes.dto.*;
import pt.florinhas.marcacoes.repository.*;
import pt.florinhas.marcacoes.service.MarcacaoService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MarcacaoController.class)
class MarcacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MarcacaoService marcacaoService;

    @MockBean
    private UtenteRepository utenteRepository;

    @MockBean
    private FuncionarioRepository funcionarioRepository;

    @MockBean
    private UtilizadorRepository utilizadorRepository;

    private Funcionario funcionario;
    private Utente utente;
    private Marcacao marcacao;

    @BeforeEach
    void setUp() {
        funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setNome("João Silva");
        funcionario.setTipo(FuncionarioTipo.SECRETARIA);

        utente = new Utente();
        utente.setId(2L);
        utente.setNome("Maria Santos");

        marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setData(LocalDateTime.now().plusDays(1));
        marcacao.setEstado(EventoEstado.AGENDADO);
        marcacao.setCriadoPor(funcionario);
    }

    @Test
    void criarMarcacaoPresencial_DeveRetornar200() throws Exception {
        // Arrange
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(LocalDateTime.now().plusDays(1));
        request.setAssunto("Consulta");
        request.setUtenteId(2L);
        request.setCriadoPorId(1L);

        when(utenteRepository.findById(2L)).thenReturn(Optional.of(utente));
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(funcionario));
        when(marcacaoService.criarMarcacaoPresencial(any(), any(), any(), any())).thenReturn(marcacao);

        // Act & Assert
        mockMvc.perform(post("/api/marcacoes/presencial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(marcacaoService, times(1)).criarMarcacaoPresencial(any(), any(), any(), any());
    }

    @Test
    void criarMarcacaoRemota_DeveRetornar200() throws Exception {
        // Arrange
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(LocalDateTime.now().plusDays(1));
        request.setAssunto("Consulta");
        request.setUtenteId(2L);

        when(utenteRepository.findById(2L)).thenReturn(Optional.of(utente));
        when(marcacaoService.criarMarcacaoRemota(any(), any(), any())).thenReturn(marcacao);

        // Act & Assert
        mockMvc.perform(post("/api/marcacoes/remota")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(marcacaoService, times(1)).criarMarcacaoRemota(any(), any(), any());
    }

    @Test
    void cancelarMarcacao_DeveRetornar200() throws Exception {
        // Arrange
        CancelarMarcacaoRequest request = new CancelarMarcacaoRequest();
        request.setMotivo("Teste");
        request.setFuncionarioId(1L);

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(funcionario));
        when(marcacaoService.findById(1L)).thenReturn(Optional.of(marcacao));
        doNothing().when(marcacaoService).cancelarMarcacao(any(), any(), any());

        // Act & Assert
        mockMvc.perform(put("/api/marcacoes/1/cancelar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(marcacaoService, times(1)).cancelarMarcacao(any(), any(), any());
    }

    @Test
    void consultarAgenda_DeveRetornar200() throws Exception {
        // Arrange
        when(marcacaoService.consultarAgenda(any(), any())).thenReturn(List.of(marcacao));

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/agenda")
                .param("dataInicio", "2025-12-09T10:00:00")
                .param("dataFim", "2025-12-16T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(marcacaoService, times(1)).consultarAgenda(any(), any());
    }

    @Test
    void procurarAgenda_DeveRetornar200() throws Exception {
        // Arrange
        when(marcacaoService.procurarAgenda(any(), any(), any(), any(), any())).thenReturn(List.of(marcacao));

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/agenda/procurar")
                .param("dataInicio", "2025-12-09T10:00:00")
                .param("dataFim", "2025-12-16T10:00:00")
                .param("estado", "AGENDADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(marcacaoService, times(1)).procurarAgenda(any(), any(), any(), any(), any());
    }

    @Test
    void atualizarEstadoMarcacao_DeveRetornar200() throws Exception {
        // Arrange
        AtualizarEstadoRequest request = new AtualizarEstadoRequest();
        request.setNovoEstado(EventoEstado.CONFIRMADO);
        request.setFuncionarioId(1L);

        when(utilizadorRepository.findById(1L)).thenReturn(Optional.of(funcionario));
        when(marcacaoService.atualizarEstadoMarcacao(any(), any(), any())).thenReturn(marcacao);

        // Act & Assert
        mockMvc.perform(put("/api/marcacoes/1/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(marcacaoService, times(1)).atualizarEstadoMarcacao(any(), any(), any());
    }

    @Test
    void obterMarcacao_DeveRetornar200() throws Exception {
        // Arrange
        when(marcacaoService.findById(1L)).thenReturn(Optional.of(marcacao));

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(marcacaoService, times(1)).findById(1L);
    }

    @Test
    void listarTodasMarcacoes_DeveRetornar200() throws Exception {
        // Arrange
        when(marcacaoService.findAll()).thenReturn(List.of(marcacao));

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(marcacaoService, times(1)).findAll();
    }

    @Test
    void consultarMarcacoesUtente_DeveRetornar200() throws Exception {
        // Arrange
        when(utenteRepository.findById(2L)).thenReturn(Optional.of(utente));
        when(marcacaoService.consultarMarcacoesUtente(any())).thenReturn(List.of(marcacao));

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/utente/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(marcacaoService, times(1)).consultarMarcacoesUtente(any());
    }

    @Test
    void consultarMarcacoesFuncionario_DeveRetornar200() throws Exception {
        // Arrange
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(funcionario));
        when(marcacaoService.consultarMarcacoesFuncionario(any())).thenReturn(List.of(marcacao));

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/funcionario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(marcacaoService, times(1)).consultarMarcacoesFuncionario(any());
    }
}
