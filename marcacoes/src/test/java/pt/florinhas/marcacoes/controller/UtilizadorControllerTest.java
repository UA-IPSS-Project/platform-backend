package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.dto.UtilizadorInfoDTO;
import pt.florinhas.common_data.dto.UtilizadorResponseDTO;
import pt.florinhas.marcacoes.dto.CreateUserRequestDTO;
import pt.florinhas.marcacoes.dto.RecoverAccountDTO;
import pt.florinhas.marcacoes.dto.TermsStatusDTO;
import pt.florinhas.marcacoes.exception.NotFoundException;
import pt.florinhas.marcacoes.service.AuthorizationService;
import pt.florinhas.marcacoes.service.TermsService;
import pt.florinhas.marcacoes.service.UtilizadorService;

class UtilizadorControllerTest {

    @Mock
    private UtilizadorService utilizadorService;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private TermsService termsService;

    private UtilizadorController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new UtilizadorController(utilizadorService, authorizationService, termsService);
    }

    private Utilizador buildUser(Long id, String nif) {
        Utilizador u = new Utente();
        u.setId(id);
        u.setNome("User");
        u.setEmail("user@test.com");
        u.setNif(nif);
        u.setTelefone("999999999");
        return u;
    }

    @Test
    @DisplayName("Deve obter utilizador por ID e retornar DTO")
    void obterUtilizadorPorId_DeveRetornarDto() {
        Utilizador u = buildUser(1L, "100000002");
        when(utilizadorService.obterUtilizadorPorId(1L)).thenReturn(u);

        ResponseEntity<UtilizadorResponseDTO> result = controller.obterUtilizadorPorId(1L);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
    }

    @Test
    @DisplayName("Deve buscar utilizador por NIF")
    void buscarPorNif_DeveRetornarDto() {
        Utilizador u = buildUser(1L, "100000002");
        when(utilizadorService.buscarPorNif("100000002")).thenReturn(Optional.of(u));

        ResponseEntity<UtilizadorResponseDTO> result = controller.buscarPorNif("100000002");

        assertEquals(200, result.getStatusCode().value());
        assertEquals("100000002", result.getBody().getNif());
    }

    @Test
    @DisplayName("buscarPorNif deve lancar NotFoundException quando o utilizador nao for encontrado")
    void buscarPorNif_DeveLancarNotFoundException_QuandoNaoEncontrado() {
        when(utilizadorService.buscarPorNif("100000002")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> controller.buscarPorNif("100000002"));
    }

    @Test
    @DisplayName("Deve atualizar utilizador e retornar DTO")
    void atualizarUtilizador_DeveRetornarDtoAtualizado() {
        Utilizador u = buildUser(1L, "100000002");
        UtilizadorInfoDTO dto = new UtilizadorInfoDTO();
        dto.setNome("Novo Nome");

        when(utilizadorService.atualizarUtilizador(1L, dto)).thenReturn(u);

        ResponseEntity<UtilizadorResponseDTO> result = controller.atualizarUtilizador(1L, dto);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("Deve contar utentes ativos")
    void contarUtentes_DeveRetornarCount() {
        when(utilizadorService.contarUtentesAtivos()).thenReturn(12L);

        ResponseEntity<Long> result = controller.contarUtentes();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(12L, result.getBody());
    }

    @Test
    @DisplayName("Deve listar todos os funcionários paginados")
    void listarTodosFuncionarios_DeveRetornarLista() {
        UtilizadorResponseDTO dto = mock(UtilizadorResponseDTO.class);
        Page<UtilizadorResponseDTO> page = new PageImpl<>(List.of(dto));
        
        when(utilizadorService.pesquisarFuncionarios(any(), any(), any(), any())).thenReturn(page);

        ResponseEntity<Page<UtilizadorResponseDTO>> result = controller.listarTodosFuncionarios(null, null, null, PageRequest.of(0, 20));

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().getContent().size());
    }

    @Test
    @DisplayName("Deve listar todos os utentes paginados")
    void listarTodosUtentes_DeveRetornarLista() {
        UtilizadorResponseDTO dto = mock(UtilizadorResponseDTO.class);
        Page<UtilizadorResponseDTO> page = new PageImpl<>(List.of(dto));
        
        when(utilizadorService.pesquisarUtentes(any(), any(), any())).thenReturn(page);

        ResponseEntity<Page<UtilizadorResponseDTO>> result = controller.listarTodosUtentes(null, null, PageRequest.of(0, 20));

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().getContent().size());
    }

    @Test
    @DisplayName("Deve listar funcionários pendentes")
    void listarFuncionariosPendentes_DeveRetornarLista() {
        UtilizadorResponseDTO dto = mock(UtilizadorResponseDTO.class);
        when(utilizadorService.listarFuncionariosPendentes()).thenReturn(List.of(dto));

        ResponseEntity<List<UtilizadorResponseDTO>> result = controller.listarFuncionariosPendentes();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
    }

    @Test
    @DisplayName("Deve aprovar funcionário")
    void aprovarFuncionario_DeveDelegarNoService() {
        ResponseEntity<Void> result = controller.aprovarFuncionario(1L);

        assertEquals(200, result.getStatusCode().value());
        verify(utilizadorService).aprovarFuncionario(1L);
    }

    @Test
    @DisplayName("Deve pesquisar por NIF para recuperação de conta")
    void pesquisarPorNifParaRecuperacao_DeveRetornarDto() {
        Utilizador u = buildUser(1L, "100000002");
        when(utilizadorService.buscarPorNif("100000002")).thenReturn(Optional.of(u));

        ResponseEntity<UtilizadorResponseDTO> result = controller.pesquisarPorNifParaRecuperacao("100000002");

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("pesquisarPorNifParaRecuperacao deve lancar NotFoundException quando o utilizador nao for encontrado")
    void pesquisarPorNifParaRecuperacao_DeveLancarNotFoundException_QuandoNaoEncontrado() {
        when(utilizadorService.buscarPorNif("100000002")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> controller.pesquisarPorNifParaRecuperacao("100000002"));
    }

    @Test
    @DisplayName("Deve criar utilizador pela secretaria")
    void criarPelaSecretaria_DeveDelegarNoService() {
        CreateUserRequestDTO req = new CreateUserRequestDTO();
        Utilizador u = buildUser(1L, "100000002");

        when(utilizadorService.criarUtilizadorPelaSecretaria(req)).thenReturn(u);

        ResponseEntity<UtilizadorResponseDTO> result = controller.criarPelaSecretaria(req);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("Deve recuperar conta")
    void recuperarConta_DeveDelegarNoService() {
        RecoverAccountDTO req = new RecoverAccountDTO();

        ResponseEntity<Void> result = controller.recuperarConta(req);

        assertEquals(200, result.getStatusCode().value());
        verify(utilizadorService).recuperarConta(req);
    }

    @Test
    @DisplayName("solicitarEliminacaoConta deve delegar no service")
    void solicitarEliminacaoConta_DeveDelegarNoService() {
        ResponseEntity<Void> result = controller.solicitarEliminacaoConta();

        assertEquals(200, result.getStatusCode().value());
        verify(utilizadorService).solicitarEliminacaoConta();
    }

    @Test
    @DisplayName("anonimizarUtilizador deve delegar no service")
    void anonimizarUtilizador_DeveDelegarNoService() {
        ResponseEntity<Void> result = controller.anonimizarUtilizador(1L);

        assertEquals(200, result.getStatusCode().value());
        verify(utilizadorService).anonimizarUtilizador(1L);
    }

    @Test
    @DisplayName("anonimizarEEliminarUtilizador deve delegar no service")
    void anonimizarEEliminarUtilizador_DeveDelegarNoService() {
        ResponseEntity<Void> result = controller.anonimizarEEliminarUtilizador(1L);

        assertEquals(200, result.getStatusCode().value());
        verify(utilizadorService).anonimizarEEliminarUtilizador(1L);
    }

    @Test
    @DisplayName("exportarDados deve retornar dados mapeados")
    void exportarDados_DeveRetornarDados() {
        Map<String, Object> mockData = Map.of("nome", "User");
        when(utilizadorService.exportarDadosUtilizador()).thenReturn(mockData);

        ResponseEntity<Map<String, Object>> result = controller.exportarDados();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(mockData, result.getBody());
    }

    @Test
    @DisplayName("verificarTermos deve retornar o status dos termos")
    void verificarTermos_DeveRetornarStatus() {
        Utilizador user = buildUser(1L, "100000002");
        TermsStatusDTO mockDto = new TermsStatusDTO(1, 1, false);
        when(utilizadorService.getUtilizadorAutenticado()).thenReturn(user);
        when(termsService.getStatus(user)).thenReturn(mockDto);

        ResponseEntity<TermsStatusDTO> result = controller.verificarTermos();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(mockDto, result.getBody());
    }

    @Test
    @DisplayName("aceitarTermos deve delegar no service")
    void aceitarTermos_DeveDelegarNoService() {
        Utilizador user = buildUser(1L, "100000002");
        when(utilizadorService.getUtilizadorAutenticado()).thenReturn(user);

        ResponseEntity<Void> result = controller.aceitarTermos(2);

        assertEquals(200, result.getStatusCode().value());
        verify(termsService).acceptTerms(user, 2);
    }

    @Test
    @DisplayName("publicarTermos deve retornar a nova versao")
    void publicarTermos_DeveRetornarNovaVersao() {
        Utilizador user = buildUser(1L, "100000002");
        when(utilizadorService.getUtilizadorAutenticado()).thenReturn(user);
        when(termsService.publishTerms("PT", "EN", "Update", 1L)).thenReturn(3);

        Map<String, String> body = Map.of("contentPt", "PT", "contentEn", "EN", "changeDescription", "Update");
        ResponseEntity<Map<String, Object>> result = controller.publicarTermos(body);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(3, result.getBody().get("version"));
    }

    @Test
    @DisplayName("atualizarVersaoTermos deve delegar no service")
    void atualizarVersaoTermos_DeveDelegarNoService() {
        ResponseEntity<Void> result = controller.atualizarVersaoTermos(3, "Update");

        assertEquals(200, result.getStatusCode().value());
        verify(termsService).updateTermsVersion(3, "Update");
    }

    @Test
    @DisplayName("obterConteudoTermosPublico deve retornar o conteudo no idioma")
    void obterConteudoTermosPublico_DeveRetornarConteudo() {
        when(termsService.getTermsContent("pt")).thenReturn("Termos PT");

        ResponseEntity<Map<String, String>> result = controller.obterConteudoTermosPublico("pt");

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("Termos PT", result.getBody().get("content"));
    }

    @Test
    @DisplayName("atualizarConteudoTermos deve validar parametros e delegar no service")
    void atualizarConteudoTermos_DeveValidarEDelegar() {
        // Case 1: Body is null
        ResponseEntity<Void> resultNull = controller.atualizarConteudoTermos("pt", null);
        assertEquals(400, resultNull.getStatusCode().value());

        // Case 2: Body lacks content key
        ResponseEntity<Void> resultNoKey = controller.atualizarConteudoTermos("pt", Map.of("other", "val"));
        assertEquals(400, resultNoKey.getStatusCode().value());

        // Case 3: Content is empty
        ResponseEntity<Void> resultEmpty = controller.atualizarConteudoTermos("pt", Map.of("content", "  "));
        assertEquals(400, resultEmpty.getStatusCode().value());

        // Case 4: Valid content
        ResponseEntity<Void> resultValid = controller.atualizarConteudoTermos("pt", Map.of("content", "Termos novos"));
        assertEquals(200, resultValid.getStatusCode().value());
        verify(termsService).updateTermsContent("pt", "Termos novos");
    }

    @Test
    @DisplayName("Classe UtilizadorController deve carregar")
    void classeDeveCarregar() {
        assertNotNull(UtilizadorController.class);
    }
}