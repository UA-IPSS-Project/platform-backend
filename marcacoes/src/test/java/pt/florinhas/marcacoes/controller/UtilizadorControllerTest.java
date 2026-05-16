package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.dto.UtilizadorInfoDTO;
import pt.florinhas.common_data.dto.UtilizadorResponseDTO;
import pt.florinhas.marcacoes.dto.CreateUserRequestDTO;
import pt.florinhas.marcacoes.dto.RecoverAccountDTO;
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
    @DisplayName("Classe UtilizadorController deve carregar")
    void classeDeveCarregar() {
        assertNotNull(UtilizadorController.class);
    }
}