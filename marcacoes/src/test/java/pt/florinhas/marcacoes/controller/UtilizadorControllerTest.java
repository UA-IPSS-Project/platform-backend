package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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
import pt.florinhas.marcacoes.service.UtilizadorService;

class UtilizadorControllerTest {

    @Mock
    private UtilizadorService utilizadorService;

    private UtilizadorController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new UtilizadorController();
        // inject field because controller uses @Autowired field
        try {
            var field = UtilizadorController.class.getDeclaredField("utilizadorService");
            field.setAccessible(true);
            field.set(controller, utilizadorService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    void obterUtilizadorPorId_DeveRetornarDto() {
        Utilizador u = buildUser(1L, "100000002");
        when(utilizadorService.obterUtilizadorPorId(1L)).thenReturn(u);

        ResponseEntity<UtilizadorResponseDTO> result = controller.obterUtilizadorPorId(1L);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
    }

    @Test
    void buscarPorNif_DeveRetornarDto() {
        Utilizador u = buildUser(1L, "100000002");
        when(utilizadorService.buscarPorNif("100000002")).thenReturn(Optional.of(u));

        ResponseEntity<UtilizadorResponseDTO> result = controller.buscarPorNif("100000002");

        assertEquals(200, result.getStatusCode().value());
        assertEquals("100000002", result.getBody().getNif());
    }

    @Test
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
    void contarUtentes_DeveRetornarCount() {
        when(utilizadorService.contarUtentesAtivos()).thenReturn(12L);

        ResponseEntity<Long> result = controller.contarUtentes();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(12L, result.getBody());
    }

    @Test
    void listarTodosFuncionarios_DeveRetornarLista() {
        UtilizadorResponseDTO dto = mock(UtilizadorResponseDTO.class);
        when(utilizadorService.listarTodosFuncionarios()).thenReturn(List.of(dto));

        ResponseEntity<List<UtilizadorResponseDTO>> result = controller.listarTodosFuncionarios();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void listarTodosUtentes_DeveRetornarLista() {
        UtilizadorResponseDTO dto = mock(UtilizadorResponseDTO.class);
        when(utilizadorService.listarTodosUtentes()).thenReturn(List.of(dto));

        ResponseEntity<List<UtilizadorResponseDTO>> result = controller.listarTodosUtentes();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void listarFuncionariosPendentes_DeveRetornarLista() {
        UtilizadorResponseDTO dto = mock(UtilizadorResponseDTO.class);
        when(utilizadorService.listarFuncionariosPendentes()).thenReturn(List.of(dto));

        ResponseEntity<List<UtilizadorResponseDTO>> result = controller.listarFuncionariosPendentes();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void aprovarFuncionario_DeveDelegarNoService() {
        ResponseEntity<Void> result = controller.aprovarFuncionario(1L);

        assertEquals(200, result.getStatusCode().value());
        verify(utilizadorService).aprovarFuncionario(1L);
    }

    @Test
    void pesquisarPorNifParaRecuperacao_DeveRetornarDto() {
        Utilizador u = buildUser(1L, "100000002");
        when(utilizadorService.buscarPorNif("100000002")).thenReturn(Optional.of(u));

        ResponseEntity<UtilizadorResponseDTO> result = controller.pesquisarPorNifParaRecuperacao("100000002");

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
    }

    @Test
    void criarPelaSecretaria_DeveDelegarNoService() {
        CreateUserRequestDTO req = new CreateUserRequestDTO();
        Utilizador u = buildUser(1L, "100000002");

        when(utilizadorService.criarUtilizadorPelaSecretaria(req)).thenReturn(u);

        ResponseEntity<UtilizadorResponseDTO> result = controller.criarPelaSecretaria(req);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
    }

    @Test
    void recuperarConta_DeveDelegarNoService() {
        RecoverAccountDTO req = new RecoverAccountDTO();

        ResponseEntity<Void> result = controller.recuperarConta(req);

        assertEquals(200, result.getStatusCode().value());
        verify(utilizadorService).recuperarConta(req);
    }
}