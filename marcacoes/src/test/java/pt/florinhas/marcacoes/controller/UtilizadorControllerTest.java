package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;
import pt.florinhas.common_data.dto.UtilizadorInfoDTO;
import pt.florinhas.common_data.dto.UtilizadorResponseDTO;
import pt.florinhas.marcacoes.dto.CreateUserRequestDTO;
import pt.florinhas.marcacoes.dto.RecoverAccountDTO;
import pt.florinhas.marcacoes.dto.TermsStatusDTO;
import pt.florinhas.marcacoes.service.AuditLogService;
import pt.florinhas.marcacoes.service.AuthorizationService;
import pt.florinhas.marcacoes.service.TermsService;
import pt.florinhas.marcacoes.service.UtilizadorService;

class UtilizadorControllerTest {

    private UtilizadorService utilizadorService;
    private AuthorizationService authorizationService;
    private AuditLogService auditLogService;
    private TermsService termsService;

    private UtilizadorController controller;

    @BeforeEach
    void setUp() throws Exception {

        utilizadorService = mock(UtilizadorService.class);
        authorizationService = mock(AuthorizationService.class);
        auditLogService = mock(AuditLogService.class);
        termsService = mock(TermsService.class);

        controller = new UtilizadorController();

        setField("utilizadorService", utilizadorService);
        setField("authorizationService", authorizationService);
        setField("auditLogService", auditLogService);
        setField("termsService", termsService);
    }

    @Test
    void obterUtilizadorPorId_DeveRetornarDto() {

        Utente user = new Utente();

        user.setId(1L);
        user.setNome("Teste");

        when(utilizadorService.obterUtilizadorPorId(1L))
                .thenReturn(user);

        ResponseEntity<UtilizadorResponseDTO> result = controller.obterUtilizadorPorId(1L);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("Teste", result.getBody().getNome());
    }

    @Test
    void buscarPorNif_DeveRetornarDto() {

        Utente user = new Utente();
        user.setNome("Teste");

        when(utilizadorService.buscarPorNif("123"))
                .thenReturn(Optional.of(user));

        ResponseEntity<UtilizadorResponseDTO> result = controller.buscarPorNif("123");
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void atualizarUtilizador_DeveRetornarDto() {

        Utente user = new Utente();
        user.setNome("Atualizado");

        when(utilizadorService.atualizarUtilizador(any(), any()))
                .thenReturn(user);

        ResponseEntity<UtilizadorResponseDTO> result = controller.atualizarUtilizador( 1L, mock(UtilizadorInfoDTO.class));
        assertEquals(200, result.getStatusCode().value());
        assertEquals("Atualizado", result.getBody().getNome());
    }

    @Test
    void contarUtentes_DeveRetornarValor() {

        when(utilizadorService.contarUtentesAtivos())
                .thenReturn(10L);

        ResponseEntity<Long> result = controller.contarUtentes();
        assertEquals(10L, result.getBody());
    }

    @Test
    void contarFuncionarios_DeveRetornarValor() {

        when(utilizadorService.contarFuncionariosAtivos())
                .thenReturn(5L);

        ResponseEntity<Long> result = controller.contarFuncionarios();
        assertEquals(5L, result.getBody());
    }

    @Test
    void listarTodosFuncionarios_DeveRetornarPagina() {

        when(utilizadorService.pesquisarFuncionarios(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        ResponseEntity<?> result = controller.listarTodosFuncionarios(null, null, null, PageRequest.of(0, 20));
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void listarTodosUtentes_DeveRetornarPagina() {

        when(utilizadorService.pesquisarUtentes(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        ResponseEntity<?> result = controller.listarTodosUtentes(null, null, PageRequest.of(0, 20));

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void listarFuncionariosPendentes_DeveRetornarLista() {

        when(utilizadorService.listarFuncionariosPendentes())
                .thenReturn(List.of());

        ResponseEntity<List<UtilizadorResponseDTO>> result = controller.listarFuncionariosPendentes();
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void aprovarFuncionario_DeveExecutarService() {

        ResponseEntity<Void> result = controller.aprovarFuncionario(1L);
        assertEquals(200, result.getStatusCode().value());
        verify(utilizadorService).aprovarFuncionario(1L);
    }

    @Test
    void criarPelaSecretaria_DeveRetornarDto() {

        Funcionario funcionario = new Funcionario();
        funcionario.setTipo(FuncionarioTipo.SECRETARIA);

        when(utilizadorService.criarUtilizadorPelaSecretaria(any()))
                .thenReturn(funcionario);

        ResponseEntity<UtilizadorResponseDTO> result = controller.criarPelaSecretaria(mock(CreateUserRequestDTO.class));
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void recuperarConta_DeveExecutarService() {

        ResponseEntity<Void> result = controller.recuperarConta(mock(RecoverAccountDTO.class));
        assertEquals(200, result.getStatusCode().value());
        verify(utilizadorService).recuperarConta(any());
    }

    @Test
    void gerarCodigoPresencial_DeveRetornarCodigo() {

        when(utilizadorService.gerarCodigoPresencial("123"))
                .thenReturn("654321");
        ResponseEntity<Map<String, String>> result = controller.gerarCodigoPresencial(Map.of("nif", "123"));
        assertEquals("654321", result.getBody().get("code"));
    }

    @Test
    void solicitarEliminacaoConta_DeveExecutarService() {

        ResponseEntity<Void> result = controller.solicitarEliminacaoConta();
        assertEquals(200, result.getStatusCode().value());
        verify(utilizadorService).solicitarEliminacaoConta();
    }

    @Test
    void anonimizarUtilizador_DeveExecutarService() {

        ResponseEntity<Void> result = controller.anonimizarUtilizador(1L);
        assertEquals(200, result.getStatusCode().value());
        verify(utilizadorService).anonimizarUtilizador(1L);
    }

    @Test
    void anonimizarEEliminarUtilizador_DeveExecutarService() {

        ResponseEntity<Void> result = controller.anonimizarEEliminarUtilizador(1L);
        assertEquals(200, result.getStatusCode().value());
        verify(utilizadorService).anonimizarEEliminarUtilizador(1L);
    }

    @Test
    void exportarDados_DeveRetornarMapa() {

        when(utilizadorService.exportarDadosUtilizador())
                .thenReturn(Map.of("a", "b"));

        ResponseEntity<Map<String, Object>> result = controller.exportarDados();

        assertEquals("b", result.getBody().get("a"));
    }

    @Test
    void verificarTermos_DeveRetornarStatus() {

        Utilizador user = new Utente();

        when(utilizadorService.getUtilizadorAutenticado())
                .thenReturn(user);

        when(termsService.getStatus(user))
                .thenReturn(mock(TermsStatusDTO.class));

        ResponseEntity<TermsStatusDTO> result = controller.verificarTermos();
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void aceitarTermos_DeveExecutarService() {

        Utilizador user = new Utente();

        when(utilizadorService.getUtilizadorAutenticado())
                .thenReturn(user);

        ResponseEntity<Void> result = controller.aceitarTermos(2);
        assertEquals(200, result.getStatusCode().value());
        verify(termsService).acceptTerms(user, 2);
    }

    @Test
    void publicarTermos_DeveRetornarVersao() {

        Utilizador user = new Utente();
        user.setId(1L);

        when(utilizadorService.getUtilizadorAutenticado())
                .thenReturn(user);

        when(termsService.publishTerms(any(), any(), any(), any()))
                .thenReturn(5);

        ResponseEntity<Map<String, Object>> result = controller.publicarTermos(Map.of("contentPt", "pt","contentEn", "en","changeDescription", "mudanças"));
        assertEquals(5, result.getBody().get("version"));
    }

    @Test
    void obterConteudoTermosPublico_DeveRetornarConteudo() {

        when(termsService.getTermsContent("pt"))
                .thenReturn("conteudo");

        ResponseEntity<Map<String, String>> result = controller.obterConteudoTermosPublico("pt");
        assertEquals("conteudo", result.getBody().get("content"));
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = UtilizadorController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }
}