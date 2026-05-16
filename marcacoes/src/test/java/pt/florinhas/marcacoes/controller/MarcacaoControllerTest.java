package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.*;
import pt.florinhas.marcacoes.service.AuthorizationService;
import pt.florinhas.marcacoes.service.MarcacaoService;

class MarcacaoControllerTest {

    @Mock
    private MarcacaoService marcacaoService;
    @Mock
    private AuthorizationService authService;

    private MarcacaoController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new MarcacaoController(marcacaoService, authService);
    }

    private Marcacao buildMarcacao(Long id) {
        Marcacao m = new Marcacao();
        m.setId(id);
        m.setData(LocalDateTime.of(2026, 4, 20, 10, 0));
        m.setEstado(EventoEstado.AGENDADO);
        m.setDuration(15);
        return m;
    }

    private MarcacaoResponseDTO buildResponseDTO(Long id, Long utenteId) {
        MarcacaoResponseDTO dto = new MarcacaoResponseDTO();
        dto.setId(id);
        dto.setData(LocalDateTime.of(2026, 4, 20, 10, 0));
        dto.setEstado(EventoEstado.AGENDADO);

        if (utenteId != null) {
            MarcacaoResponseDTO.MarcacaoSecretariaDTO sec = new MarcacaoResponseDTO.MarcacaoSecretariaDTO();
            MarcacaoResponseDTO.UtenteDTO ut = new MarcacaoResponseDTO.UtenteDTO();
            ut.setId(utenteId);
            sec.setUtente(ut);
            dto.setMarcacaoSecretaria(sec);
        }
        return dto;
    }

    @Test
    @DisplayName("Deve contar marcações diárias")
    void contarMarcacoesHoje_DeveRetornarCount() {
        when(marcacaoService.contarMarcacoesDiarias(any(LocalDateTime.class))).thenReturn(7L);
        ResponseEntity<Long> result = controller.contarMarcacoesHoje();
        assertEquals(200, result.getStatusCode().value());
        assertEquals(7L, result.getBody());
    }

    @Test
    @DisplayName("Deve criar marcação presencial e retornar ID e estado")
    void criarMarcacaoPresencial_DeveRetornarMapaComDados() {
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        Marcacao marcacao = buildMarcacao(1L);
        when(marcacaoService.criarMarcacaoPresencial(request)).thenReturn(marcacao);

        ResponseEntity<Map<String, String>> result = controller.criarMarcacaoPresencial(request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("1", result.getBody().get("id"));
        assertEquals("AGENDADO", result.getBody().get("estado"));
    }

    @Test
    @DisplayName("Deve criar marcação remota e retornar ID e estado")
    void criarMarcacaoRemota_DeveRetornarMapaComDados() {
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        Marcacao marcacao = buildMarcacao(2L);
        when(marcacaoService.criarMarcacaoRemota(request)).thenReturn(marcacao);

        ResponseEntity<Map<String, String>> result = controller.criarMarcacaoRemota(request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("2", result.getBody().get("id"));
        assertEquals("AGENDADO", result.getBody().get("estado"));
    }

    @Test
    @DisplayName("Deve criar marcação balneário com sucesso")
    void criarMarcacaoBalneario_DeveRetornarMapaDetalhado() {
        CriarMarcacaoBalnearioRequest request = new CriarMarcacaoBalnearioRequest();
        Marcacao marcacao = buildMarcacao(3L);
        marcacao.setDuration(45);
        when(marcacaoService.criarMarcacaoBalneario(request)).thenReturn(marcacao);

        ResponseEntity<Map<String, String>> result = controller.criarMarcacaoBalneario(request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("3", result.getBody().get("id"));
        assertEquals("45", result.getBody().get("duration"));
        assertEquals("AGENDADO", result.getBody().get("estado"));
    }

    @Test
    @DisplayName("Deve atualizar detalhes balneário")
    void atualizarDetalhesBalneario_DeveRetornarResponseDto() {
        CriarMarcacaoBalnearioRequest request = new CriarMarcacaoBalnearioRequest();
        request.setProdutosHigiene(true);
        request.setLavagemRoupa(true);
        request.setRoupas(new ArrayList<>());

        MarcacaoResponseDTO mockResponse = buildResponseDTO(3L, null);
        when(marcacaoService.atualizarDetalhesBalneario(eq(3L), any(), any(), any())).thenReturn(mockResponse);

        ResponseEntity<MarcacaoResponseDTO> result = controller.atualizarDetalhesBalneario(3L, request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(3L, result.getBody().getId());
    }

    @Test
    @DisplayName("Deve falhar consulta de agenda quando não é admin")
    void consultarAgenda_DeveFalharQuandoNaoAdmin() {
        when(authService.isAdmin()).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> controller.consultarAgenda(null, null, "SECRETARIA"));
    }

    @Test
    @DisplayName("Deve consultar agenda quando admin")
    void consultarAgenda_DeveRetornarListaQuandoAdmin() {
        List<MarcacaoResponseDTO> list = List.of(buildResponseDTO(1L, null));
        when(authService.isAdmin()).thenReturn(true);
        when(marcacaoService.consultarAgenda(any(), any(), any())).thenReturn(list);

        ResponseEntity<List<MarcacaoResponseDTO>> result = controller.consultarAgenda(null, null, "SECRETARIA");

        assertEquals(200, result.getStatusCode().value());
        assertEquals(list, result.getBody());
    }

    @Test
    @DisplayName("procurarAgenda deve falhar quando nao for admin e consultar dados de outro utente")
    void procurarAgenda_DeveFalharQuandoNaoAdminEConsultarOutroUtente() {
        when(authService.getCurrentUserId()).thenReturn(10L);
        when(authService.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> controller.procurarAgenda(null, null, null, 11L, null));
    }

    @Test
    @DisplayName("procurarAgenda deve permitir quando admin ou consultando proprio utente")
    void procurarAgenda_DevePermitirQuandoAdminOuProprioUtente() {
        List<MarcacaoResponseDTO> list = List.of(buildResponseDTO(1L, null));
        when(authService.getCurrentUserId()).thenReturn(10L);
        when(authService.isAdmin()).thenReturn(false);
        when(marcacaoService.procurarAgenda(any(), any(), any(), eq(10L), any())).thenReturn(list);

        ResponseEntity<List<MarcacaoResponseDTO>> result = controller.procurarAgenda(null, null, null, 10L, null);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(list, result.getBody());
    }

    @Test
    @DisplayName("atualizarEstadoMarcacao deve permitir quando admin ou proprietario da marcacao")
    void atualizarEstadoMarcacao_DevePermitirQuandoAdminOuProprietario() {
        AtualizarEstadoRequest request = new AtualizarEstadoRequest();
        request.setNovoEstado("CONCLUIDO");
        request.setMotivoCancelamento("Ok");
        MarcacaoResponseDTO existing = buildResponseDTO(1L, 10L);
        when(authService.isAdmin()).thenReturn(false);
        when(marcacaoService.obterMarcacaoDTO(1L)).thenReturn(existing);
        when(marcacaoService.atualizarEstadoMarcacao(eq(1L), any())).thenReturn(existing);

        ResponseEntity<MarcacaoResponseDTO> result = controller.atualizarEstadoMarcacao(1L, request);

        assertEquals(200, result.getStatusCode().value());
        verify(authService).checkPermission(10L, "alterar esta marcação");
    }

    @Test
    @DisplayName("atualizarEstadoMarcacao deve falhar quando nao proprietario")
    void atualizarEstadoMarcacao_DeveFalharQuandoNaoProprietario() {
        AtualizarEstadoRequest request = new AtualizarEstadoRequest();
        request.setNovoEstado("CONCLUIDO");
        request.setMotivoCancelamento("Ok");
        MarcacaoResponseDTO existing = buildResponseDTO(1L, null); // No utente context
        when(authService.isAdmin()).thenReturn(false);
        when(marcacaoService.obterMarcacaoDTO(1L)).thenReturn(existing);

        assertThrows(AccessDeniedException.class, () -> controller.atualizarEstadoMarcacao(1L, request));
    }

    @Test
    @DisplayName("consultarMarcacoesPassadas deve delegar no service")
    void consultarMarcacoesPassadas_DeveDelegarNoService() {
        Page<MarcacaoResponseDTO> page = new PageImpl<>(List.of(buildResponseDTO(1L, null)));
        when(authService.getCurrentUserId()).thenReturn(10L);
        when(authService.isAdmin()).thenReturn(false);
        when(marcacaoService.consultarMarcacoesPassadasPaginated(any(), any(), eq(10L), any(), any(), any(), any()))
                .thenReturn(page);

        ResponseEntity<Page<MarcacaoResponseDTO>> result = controller.consultarMarcacoesPassadas(null, null, 10L, null,
                null, null, PageRequest.of(0, 20));

        assertEquals(200, result.getStatusCode().value());
        assertEquals(page, result.getBody());
    }

    @Test
    @DisplayName("notificarDocumentosInvalidos deve delegar no service")
    void notificarDocumentosInvalidos_DeveDelegarNoService() {
        NotificarDocumentosRequest request = new NotificarDocumentosRequest();
        MarcacaoResponseDTO mockResponse = buildResponseDTO(1L, null);
        when(marcacaoService.notificarDocumentosInvalidos(1L, request)).thenReturn(mockResponse);

        ResponseEntity<MarcacaoResponseDTO> result = controller.notificarDocumentosInvalidos(1L, request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(mockResponse, result.getBody());
    }

    @Test
    @DisplayName("consultarMarcacoesUtente deve delegar no service se tiver permissao")
    void consultarMarcacoesUtente_DeveDelegarNoService() {
        List<MarcacaoResponseDTO> list = List.of(buildResponseDTO(1L, null));
        when(marcacaoService.consultarMarcacoesUtente(10L)).thenReturn(list);

        ResponseEntity<List<MarcacaoResponseDTO>> result = controller.consultarMarcacoesUtente(10L);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(list, result.getBody());
        verify(authService).checkPermission(10L, "consultar dados de outro utente");
    }

    @Test
    @DisplayName("consultarMarcacoesBloqueadas deve delegar no service")
    void consultarMarcacoesBloqueadas_DeveDelegarNoService() {
        List<Map<String, Object>> mockList = List.of(Map.of("id", 1L));
        when(marcacaoService.consultarMarcacoesBloqueadas(10L)).thenReturn(mockList);

        ResponseEntity<List<Map<String, Object>>> result = controller.consultarMarcacoesBloqueadas(10L);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(mockList, result.getBody());
    }

    @Test
    @DisplayName("consultarMarcacoesFuncionario deve delegar no service")
    void consultarMarcacoesFuncionario_DeveDelegarNoService() {
        List<MarcacaoResponseDTO> list = List.of(buildResponseDTO(1L, null));
        when(marcacaoService.consultarMarcacoesFuncionario(20L)).thenReturn(list);

        ResponseEntity<List<MarcacaoResponseDTO>> result = controller.consultarMarcacoesFuncionario(20L);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(list, result.getBody());
        verify(authService).checkPermission(20L, "consultar marcações deste funcionário");
    }

    @Test
    @DisplayName("obterMarcacao deve retornar NotFound quando nula")
    void obterMarcacao_DeveRetornar404_QuandoNula() {
        when(marcacaoService.obterMarcacaoDTO(1L)).thenReturn(null);

        ResponseEntity<MarcacaoResponseDTO> result = controller.obterMarcacao(1L);

        assertEquals(404, result.getStatusCode().value());
    }

    @Test
    @DisplayName("obterMarcacao deve retornar detalhes quando proprietario ou admin")
    void obterMarcacao_DeveRetornarDetalhes() {
        MarcacaoResponseDTO existing = buildResponseDTO(1L, 10L);
        when(authService.isAdmin()).thenReturn(false);
        when(marcacaoService.obterMarcacaoDTO(1L)).thenReturn(existing);

        ResponseEntity<MarcacaoResponseDTO> result = controller.obterMarcacao(1L);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(existing, result.getBody());
        verify(authService).checkPermission(10L, "visualizar esta marcação");
    }

    @Test
    @DisplayName("Deve listar todas as marcações paginadas para admin")
    void listarTodasMarcacoes_DeveRetornarPaginaQuandoAdmin() {
        Page<MarcacaoResponseDTO> page = new PageImpl<>(List.of(new MarcacaoResponseDTO()));
        when(authService.isAdmin()).thenReturn(true);
        when(marcacaoService.listarTodasMarcacoesPaginated(any())).thenReturn(page);

        ResponseEntity<Page<MarcacaoResponseDTO>> result = controller.listarTodasMarcacoes(PageRequest.of(0, 20));

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("Deve falhar listagem geral de marcacoes se nao for admin")
    void listarTodasMarcacoes_DeveFalharSeNaoAdmin() {
        when(authService.isAdmin()).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> controller.listarTodasMarcacoes(PageRequest.of(0, 20)));
    }

    @Test
    @DisplayName("reservarSlotTemporariamente deve delegar no service")
    void reservarSlotTemporariamente_DeveDelegarNoService() {
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        when(marcacaoService.criarReservaTemporaria(request)).thenReturn(100L);

        ResponseEntity<Map<String, Object>> result = controller.reservarSlotTemporariamente(request);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(100L, result.getBody().get("tempId"));
    }

    @Test
    @DisplayName("Deve libertar slot de reserva temporária")
    void libertarSlot_DeveDelegarNoService() {
        ResponseEntity<Void> result = controller.libertarSlot(55L);
        assertEquals(200, result.getStatusCode().value());
        verify(marcacaoService).apagarReservaTemporaria(55L);
    }

    @Test
    @DisplayName("reagendarMarcacao deve delegar no service")
    void reagendarMarcacao_DeveDelegarNoService() {
        ReagendarMarcacaoRequest request = new ReagendarMarcacaoRequest();
        MarcacaoResponseDTO mockResponse = buildResponseDTO(1L, null);
        when(marcacaoService.reagendarMarcacao(1L, request)).thenReturn(mockResponse);

        ResponseEntity<MarcacaoResponseDTO> result = controller.reagendarMarcacao(1L, request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(mockResponse, result.getBody());
    }

    @Test
    @DisplayName("getBalnearioFrequenciaEstatisticas deve falhar se nao for admin")
    void getBalnearioFrequenciaEstatisticas_DeveFalharSeNaoAdmin() {
        when(authService.isAdmin()).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> controller.getBalnearioFrequenciaEstatisticas("MES"));
    }

    @Test
    @DisplayName("getBalnearioFrequenciaEstatisticas deve retornar dados de frequencia")
    void getBalnearioFrequenciaEstatisticas_DeveRetornarDadosQuandoAdmin() {
        BalnearioAttendanceStatsDTO stats = new BalnearioAttendanceStatsDTO();
        when(authService.isAdmin()).thenReturn(true);
        when(marcacaoService.obterEstatisticasFrequenciaBalneario("MES")).thenReturn(stats);

        ResponseEntity<BalnearioAttendanceStatsDTO> result = controller.getBalnearioFrequenciaEstatisticas("MES");

        assertEquals(200, result.getStatusCode().value());
        assertEquals(stats, result.getBody());
    }

    @Test
    @DisplayName("Classe MarcacaoController deve carregar")
    void classeDeveCarregar() {
        assertNotNull(MarcacaoController.class);
    }
}