package pt.florinhas.marcacoes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoBalneario;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Valencia;
import pt.florinhas.marcacoes.dto.CriarBalnearioRequest;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.service.MarcacaoService;
import pt.florinhas.marcacoes.repository.UtenteRepository;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;
import pt.florinhas.marcacoes.repository.ValenciaRepository;

@WebMvcTest(MarcacaoController.class)
class MarcacaoControllerIntegrationTest {

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
    private ValenciaRepository valenciaRepository;

    @Test
    void testCriarMarcacaoPresencial_Success() throws Exception {
        // Arrange
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(LocalDate.now().plusDays(1));
        request.setHora(LocalTime.of(10, 0));
        request.setTipoAtendimento("PRESENCIAL");
        request.setUtenteId(1L);
        request.setFuncionarioId(1L);
        request.setValenciaId(1L);
        request.setCriadoPorId(2L);

        Utente utente = new Utente();
        utente.setId(1L);
        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);
        Valencia valencia = new Valencia();
        valencia.setId(1L);
        Funcionario secretaria = new Funcionario();
        secretaria.setId(2L);

        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setData(request.getData());
        marcacao.setHora(request.getHora());

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(funcionario));
        when(valenciaRepository.findById(1L)).thenReturn(Optional.of(valencia));
        when(funcionarioRepository.findById(2L)).thenReturn(Optional.of(secretaria));
        when(marcacaoService.criarMarcacaoPresencial(any(), any(), any(), any(), any(), any(), any())).thenReturn(marcacao);

        // Act & Assert
        mockMvc.perform(post("/api/marcacoes/presencial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testCriarMarcacaoRemota_Success() throws Exception {
        // Arrange
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        request.setData(LocalDate.now().plusDays(2));
        request.setHora(LocalTime.of(14, 30));
        request.setTipoAtendimento("REMOTO");
        request.setUtenteId(1L);
        request.setFuncionarioId(1L);
        request.setValenciaId(1L);

        Utente utente = new Utente();
        utente.setId(1L);
        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);
        Valencia valencia = new Valencia();
        valencia.setId(1L);

        Marcacao marcacao = new Marcacao();
        marcacao.setId(2L);
        marcacao.setData(request.getData());
        marcacao.setHora(request.getHora());

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(funcionario));
        when(valenciaRepository.findById(1L)).thenReturn(Optional.of(valencia));
        when(marcacaoService.criarMarcacaoRemota(any(), any(), any(), any(), any(), any())).thenReturn(marcacao);

        // Act & Assert
        mockMvc.perform(post("/api/marcacoes/remota")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void testConsultarAgenda_Success() throws Exception {
        // Arrange
        Marcacao marcacao1 = new Marcacao();
        marcacao1.setId(1L);
        marcacao1.setData(LocalDate.now().plusDays(1));

        Marcacao marcacao2 = new Marcacao();
        marcacao2.setId(2L);
        marcacao2.setData(LocalDate.now().plusDays(1));

        when(marcacaoService.consultarAgenda(any(), any(), any(), any(), any()))
                .thenReturn(Arrays.asList(marcacao1, marcacao2));

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/agenda")
                .param("dataInicio", LocalDate.now().toString())
                .param("dataFim", LocalDate.now().plusDays(7).toString())
                .param("estado", "AGENDADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void testCriarMarcacaoBalneario_Success() throws Exception {
        // Arrange
        CriarBalnearioRequest request = new CriarBalnearioRequest();
        request.setData(LocalDate.now().plusDays(1));
        request.setHora(LocalTime.of(9, 0));
        request.setUtenteId(1L);
        request.setFuncionarioId(1L);
        request.setTipoCriador("TECNICO");
        request.setProdutosHigiene(true);
        request.setLavagemRoupa(false);
        request.setRoupaDescricao("Roupa interior");

        Utente utente = new Utente();
        utente.setId(1L);
        Funcionario tecnico = new Funcionario();
        tecnico.setId(1L);

        MarcacaoBalneario marcacao = new MarcacaoBalneario();
        marcacao.setId(1L);
        marcacao.setData(request.getData());
        marcacao.setProdutosHigiene(true);

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(tecnico));
        when(marcacaoService.criarMarcacaoBalnearioTecnico(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(marcacao);

        // Act & Assert
        mockMvc.perform(post("/api/marcacoes/balneario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.produtosHigiene").value(true));
    }

    @Test
    void testObterMarcacao_Success() throws Exception {
        // Arrange
        Marcacao marcacao = new Marcacao();
        marcacao.setId(1L);
        marcacao.setData(LocalDate.now().plusDays(1));
        marcacao.setEstado("AGENDADO");

        when(marcacaoService.findById(1L)).thenReturn(Optional.of(marcacao));

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("AGENDADO"));
    }

    @Test
    void testListarTodasMarcacoes_Success() throws Exception {
        // Arrange
        Marcacao marcacao1 = new Marcacao();
        marcacao1.setId(1L);
        Marcacao marcacao2 = new Marcacao();
        marcacao2.setId(2L);

        when(marcacaoService.findAll()).thenReturn(Arrays.asList(marcacao1, marcacao2));

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void testConsultarMarcacoesUtente_Success() throws Exception {
        // Arrange
        Marcacao marcacao1 = new Marcacao();
        marcacao1.setId(1L);
        Marcacao marcacao2 = new Marcacao();
        marcacao2.setId(2L);

        Utente utente = new Utente();
        utente.setId(1L);

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(marcacaoService.consultarMarcacoesUtente(utente)).thenReturn(Arrays.asList(marcacao1, marcacao2));

        // Act & Assert
        mockMvc.perform(get("/api/marcacoes/utente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }
}