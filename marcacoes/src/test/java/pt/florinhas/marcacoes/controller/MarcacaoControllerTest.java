package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.dto.CriarMarcacaoRequest;
import pt.florinhas.marcacoes.dto.MarcacaoResponseDTO;
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
    @DisplayName("Deve falhar consulta de agenda quando não é admin")
    void consultarAgenda_DeveFalharQuandoNaoAdmin() {
        when(authService.isAdmin()).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> controller.consultarAgenda(null, null, "SECRETARIA"));
    }

    @Test
    @DisplayName("Deve listar todas as marcações paginadas para admin")
    void listarTodasMarcacoes_DeveRetornarPaginaQuandoAdmin() {
        Page<MarcacaoResponseDTO> page = new PageImpl<>(List.of(new MarcacaoResponseDTO()));
        when(authService.isAdmin()).thenReturn(true);
        when(marcacaoService.listarTodasMarcacoesPaginated(null)).thenReturn(page);

        ResponseEntity<Page<MarcacaoResponseDTO>> result = controller.listarTodasMarcacoes(null);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("Deve libertar slot de reserva temporária")
    void libertarSlot_DeveDelegarNoService() {
        ResponseEntity<Void> result = controller.libertarSlot(55L);
        assertEquals(200, result.getStatusCode().value());
        verify(marcacaoService).apagarReservaTemporaria(55L);
    }

    @Test
    @DisplayName("Classe MarcacaoController deve carregar")
    void classeDeveCarregar() {
        assertNotNull(MarcacaoController.class);
    }
}