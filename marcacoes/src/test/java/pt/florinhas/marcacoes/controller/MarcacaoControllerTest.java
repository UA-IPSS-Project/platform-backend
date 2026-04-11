package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.AtualizarEstadoRequest;
import pt.florinhas.marcacoes.dto.BalnearioAttendanceStatsDTO;
import pt.florinhas.marcacoes.dto.CriarMarcacaoBalnearioRequest;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.MarcacaoResponseDTO;
import pt.florinhas.marcacoes.dto.NotificarDocumentosRequest;
import pt.florinhas.marcacoes.dto.ReagendarMarcacaoRequest;
import pt.florinhas.marcacoes.service.AuthService;
import pt.florinhas.marcacoes.service.MarcacaoService;

class MarcacaoControllerTest {

    @Mock
    private MarcacaoService marcacaoService;

    @Mock
    private AuthService authService;

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

    @Test
    void contarMarcacoesHoje_DeveRetornarCount() {
        when(marcacaoService.contarMarcacoesDiarias(any(LocalDateTime.class))).thenReturn(7L);

        ResponseEntity<Long> result = controller.contarMarcacoesHoje();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(7L, result.getBody());
    }

    @Test
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
    void criarMarcacaoRemota_DeveRetornarMapaComDados() {
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();
        Marcacao marcacao = buildMarcacao(2L);

        when(marcacaoService.criarMarcacaoRemota(request)).thenReturn(marcacao);

        ResponseEntity<Map<String, String>> result = controller.criarMarcacaoRemota(request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("2", result.getBody().get("id"));
    }

    @Test
    void criarMarcacaoBalneario_DeveRetornarMapaComDuration() {
        CriarMarcacaoBalnearioRequest request = mock(CriarMarcacaoBalnearioRequest.class);
        Marcacao marcacao = buildMarcacao(3L);
        marcacao.setDuration(30);

        when(marcacaoService.criarMarcacaoBalneario(request)).thenReturn(marcacao);

        ResponseEntity<Map<String, String>> result = controller.criarMarcacaoBalneario(request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("30", result.getBody().get("duration"));
        assertEquals("Marcação de balneário registada com sucesso", result.getBody().get("message"));
    }

    @Test
    void atualizarDetalhesBalneario_DeveDelegarNoService() {
        CriarMarcacaoBalnearioRequest request = mock(CriarMarcacaoBalnearioRequest.class);
        MarcacaoResponseDTO dto = new MarcacaoResponseDTO();

        when(request.getProdutosHigiene()).thenReturn(true);
        when(request.getLavagemRoupa()).thenReturn(false);
        when(request.getRoupas()).thenReturn(List.of());
        when(marcacaoService.atualizarDetalhesBalneario(1L, true, false, List.of())).thenReturn(dto);

        ResponseEntity<MarcacaoResponseDTO> result = controller.atualizarDetalhesBalneario(1L, request);

        assertEquals(200, result.getStatusCode().value());
        assertSame(dto, result.getBody());
    }

    @Test
    void consultarAgenda_DeveFalharQuandoNaoAdmin() {
        when(authService.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.consultarAgenda(null, null, "SECRETARIA"));
    }

    @Test
    void consultarAgenda_DeveRetornarListaQuandoAdmin() {
        when(authService.isAdmin()).thenReturn(true);
        when(marcacaoService.consultarAgenda(null, null, "SECRETARIA")).thenReturn(List.of(new MarcacaoResponseDTO()));

        ResponseEntity<List<MarcacaoResponseDTO>> result = controller.consultarAgenda(null, null, "SECRETARIA");

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void procurarAgenda_DeveForcarUtenteAtualQuandoNaoAdmin() {
        when(authService.isAdmin()).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(10L);
        when(marcacaoService.procurarAgenda(null, null, null, 10L, null)).thenReturn(List.of());

        ResponseEntity<List<MarcacaoResponseDTO>> result = controller.procurarAgenda(null, null, null, null, null);

        assertEquals(200, result.getStatusCode().value());
        verify(marcacaoService).procurarAgenda(null, null, null, 10L, null);
    }

    @Test
    void procurarAgenda_DeveFalharQuandoNaoAdminEQuerOutroUtente() {
        when(authService.isAdmin()).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(10L);

        assertThrows(AccessDeniedException.class,
                () -> controller.procurarAgenda(null, null, null, 99L, null));
    }

    @Test
    void atualizarEstadoMarcacao_DeveDelegarQuandoAdmin() {
        AtualizarEstadoRequest request = mock(AtualizarEstadoRequest.class);
        MarcacaoResponseDTO dto = new MarcacaoResponseDTO();

        when(authService.isAdmin()).thenReturn(true);
        when(marcacaoService.atualizarEstadoMarcacao(1L, request)).thenReturn(dto);

        ResponseEntity<MarcacaoResponseDTO> result = controller.atualizarEstadoMarcacao(1L, request);

        assertEquals(200, result.getStatusCode().value());
        assertSame(dto, result.getBody());
    }

    @Test
    void atualizarEstadoMarcacao_DeveFalharQuandoNaoProprietario() {
        AtualizarEstadoRequest request = mock(AtualizarEstadoRequest.class);
        MarcacaoResponseDTO existing = Mockito.mock(MarcacaoResponseDTO.class, Mockito.RETURNS_DEEP_STUBS);

        when(authService.isAdmin()).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(10L);
        when(marcacaoService.obterMarcacaoDTO(1L)).thenReturn(existing);
        when(existing.getMarcacaoSecretaria().getUtente().getId()).thenReturn(99L);

        assertThrows(AccessDeniedException.class,
                () -> controller.atualizarEstadoMarcacao(1L, request));
    }

    @Test
    void consultarMarcacoesPassadas_DeveDelegarComUtenteAtualQuandoNaoAdmin() {
        when(authService.isAdmin()).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(5L);
        when(marcacaoService.consultarMarcacoesPassadas(null, null, 5L, EventoEstado.CONCLUIDO))
                .thenReturn(List.of());

        ResponseEntity<List<MarcacaoResponseDTO>> result =
                controller.consultarMarcacoesPassadas(null, null, null, EventoEstado.CONCLUIDO);

        assertEquals(200, result.getStatusCode().value());
        verify(marcacaoService).consultarMarcacoesPassadas(null, null, 5L, EventoEstado.CONCLUIDO);
    }

    @Test
    void notificarDocumentosInvalidos_DeveDelegarNoService() {
        NotificarDocumentosRequest request = mock(NotificarDocumentosRequest.class);
        MarcacaoResponseDTO dto = new MarcacaoResponseDTO();

        when(marcacaoService.notificarDocumentosInvalidos(1L, request)).thenReturn(dto);

        ResponseEntity<MarcacaoResponseDTO> result = controller.notificarDocumentosInvalidos(1L, request);

        assertEquals(200, result.getStatusCode().value());
        assertSame(dto, result.getBody());
    }

    @Test
    void consultarMarcacoesUtente_DeveFalharQuandoNaoEhProprietarioNemAdmin() {
        when(authService.isAdmin()).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(10L);

        assertThrows(AccessDeniedException.class,
                () -> controller.consultarMarcacoesUtente(99L));
    }

    @Test
    void consultarMarcacoesUtente_DeveDelegarQuandoProprietario() {
        when(authService.isAdmin()).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(10L);
        when(marcacaoService.consultarMarcacoesUtente(10L)).thenReturn(List.of());

        ResponseEntity<List<MarcacaoResponseDTO>> result = controller.consultarMarcacoesUtente(10L);

        assertEquals(200, result.getStatusCode().value());
        verify(marcacaoService).consultarMarcacoesUtente(10L);
    }

    @Test
    void consultarMarcacoesBloqueadas_DeveDelegarQuandoProprietario() {
        when(authService.isAdmin()).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(10L);
        when(marcacaoService.consultarMarcacoesBloqueadas(10L)).thenReturn(List.of());

        ResponseEntity<List<Map<String, Object>>> result = controller.consultarMarcacoesBloqueadas(10L);

        assertEquals(200, result.getStatusCode().value());
        verify(marcacaoService).consultarMarcacoesBloqueadas(10L);
    }

    @Test
    void consultarMarcacoesFuncionario_DeveDelegarQuandoProprietario() {
        when(authService.isAdmin()).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(10L);
        when(marcacaoService.consultarMarcacoesFuncionario(10L)).thenReturn(List.of());

        ResponseEntity<List<MarcacaoResponseDTO>> result = controller.consultarMarcacoesFuncionario(10L);

        assertEquals(200, result.getStatusCode().value());
        verify(marcacaoService).consultarMarcacoesFuncionario(10L);
    }

    @Test
    void obterMarcacao_DeveRetornar404QuandoNaoExiste() {
        when(marcacaoService.obterMarcacaoDTO(1L)).thenReturn(null);

        ResponseEntity<MarcacaoResponseDTO> result = controller.obterMarcacao(1L);

        assertEquals(404, result.getStatusCode().value());
    }

    @Test
    void obterMarcacao_DeveFalharQuandoNaoAdminENaoProprietario() {
        MarcacaoResponseDTO dto = Mockito.mock(MarcacaoResponseDTO.class, Mockito.RETURNS_DEEP_STUBS);

        when(authService.isAdmin()).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(10L);
        when(marcacaoService.obterMarcacaoDTO(1L)).thenReturn(dto);
        when(dto.getMarcacaoSecretaria().getUtente().getId()).thenReturn(99L);

        assertThrows(AccessDeniedException.class,
                () -> controller.obterMarcacao(1L));
    }

    @Test
    void listarTodasMarcacoes_DeveFalharQuandoNaoAdmin() {
        when(authService.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.listarTodasMarcacoes(null));
    }

    @Test
    void listarTodasMarcacoes_DeveRetornarPaginaQuandoAdmin() {
        Page<MarcacaoResponseDTO> page = new PageImpl<>(List.of(new MarcacaoResponseDTO()));

        when(authService.isAdmin()).thenReturn(true);
        when(marcacaoService.listarTodasMarcacoesPaginated(null)).thenReturn(page);

        ResponseEntity<Page<MarcacaoResponseDTO>> result = controller.listarTodasMarcacoes(null);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().getContent().size());
    }

    @Test
    void reservarSlotTemporariamente_DeveRetornarTempId() {
        CriarMarcacaoRequest request = new CriarMarcacaoRequest();

        when(marcacaoService.criarReservaTemporaria(request)).thenReturn(123L);

        ResponseEntity<Map<String, Object>> result = controller.reservarSlotTemporariamente(request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(123L, result.getBody().get("tempId"));
    }

    @Test
    void libertarSlot_DeveDelegarNoService() {
        ResponseEntity<?> result = controller.libertarSlot(55L);

        assertEquals(200, result.getStatusCode().value());
        verify(marcacaoService).apagarReservaTemporaria(55L);
    }

    @Test
    void reagendarMarcacao_DeveDelegarNoService() {
        ReagendarMarcacaoRequest request = new ReagendarMarcacaoRequest();
        MarcacaoResponseDTO dto = new MarcacaoResponseDTO();

        when(marcacaoService.reagendarMarcacao(1L, request)).thenReturn(dto);

        ResponseEntity<MarcacaoResponseDTO> result = controller.reagendarMarcacao(1L, request);

        assertEquals(200, result.getStatusCode().value());
        assertSame(dto, result.getBody());
    }

    @Test
    void getBalnearioFrequenciaEstatisticas_DeveFalharQuandoNaoAdmin() {
        when(authService.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> controller.getBalnearioFrequenciaEstatisticas("MES"));
    }

    @Test
    void getBalnearioFrequenciaEstatisticas_DeveDelegarQuandoAdmin() {
        BalnearioAttendanceStatsDTO dto = mock(BalnearioAttendanceStatsDTO.class);

        when(authService.isAdmin()).thenReturn(true);
        when(marcacaoService.obterEstatisticasFrequenciaBalneario("MES")).thenReturn(dto);

        ResponseEntity<BalnearioAttendanceStatsDTO> result =
                controller.getBalnearioFrequenciaEstatisticas("MES");

        assertEquals(200, result.getStatusCode().value());
        assertSame(dto, result.getBody());
    }
}