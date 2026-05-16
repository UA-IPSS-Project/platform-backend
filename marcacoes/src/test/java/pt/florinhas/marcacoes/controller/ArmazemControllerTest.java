package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import pt.florinhas.marcacoes.dto.ConsumoEstatisticaDTO;
import pt.florinhas.marcacoes.dto.ItemArmazemDTO;
import pt.florinhas.marcacoes.service.ArmazemService;
import pt.florinhas.marcacoes.service.AuditLogService;

class ArmazemControllerTest {

    @Mock
    private ArmazemService armazemService;
    @Mock
    private AuditLogService auditLogService;

    private ArmazemController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new ArmazemController(armazemService, auditLogService);
    }

    @Test
    @DisplayName("Deve listar todos os itens do armazém")
    void listarTodos_DeveRetornarLista() {
        ItemArmazemDTO dto = new ItemArmazemDTO();
        dto.setNome("Champô");
        when(armazemService.listarTodos()).thenReturn(List.of(dto));

        ResponseEntity<List<ItemArmazemDTO>> result = controller.listarTodos();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
        assertEquals("Champô", result.getBody().get(0).getNome());
    }

    @Test
    @DisplayName("Deve listar itens por categoria (case-insensitive)")
    void listarPorCategoria_DeveConverterCategoriaParaUppercase() {
        when(armazemService.listarPorCategoria("HIGIENE")).thenReturn(List.of());
        controller.listarPorCategoria("higiene");
        verify(armazemService).listarPorCategoria("HIGIENE");
    }

    @Test
    @DisplayName("Deve criar item no armazém e registar audit log")
    void criarItem_DeveRetornarItemCriadoERegistarAuditLog() {
        ItemArmazemDTO dto = new ItemArmazemDTO();
        dto.setId(10L);
        dto.setNome("Champô");
        dto.setQuantidade(100);

        when(armazemService.criarItem(dto)).thenReturn(dto);

        ResponseEntity<ItemArmazemDTO> result = controller.criarItem(dto);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(10L, result.getBody().getId());
        verify(auditLogService).log(eq("CRIAR_ITEM_ARMAZEM"), eq("ITEM_ARMAZEM"), eq(10L), anyString());
    }

    @Test
    @DisplayName("Deve atualizar item no armazém e registar audit log")
    void atualizarItem_DeveRetornarItemAtualizadoERegistarAuditLog() {
        ItemArmazemDTO dto = new ItemArmazemDTO();
        dto.setId(10L);
        dto.setNome("Champô");
        dto.setQuantidade(150);

        when(armazemService.atualizarItem(10L, dto)).thenReturn(dto);

        ResponseEntity<ItemArmazemDTO> result = controller.atualizarItem(10L, dto);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(150, result.getBody().getQuantidade());
        verify(auditLogService).log(eq("ATUALIZAR_ITEM_ARMAZEM"), eq("ITEM_ARMAZEM"), eq(10L), anyString());
    }

    @Test
    @DisplayName("Deve eliminar item e retornar No Content")
    void eliminarItem_DeveRetornarNoContent() {
        ResponseEntity<Void> result = controller.eliminarItem(1L);
        assertEquals(204, result.getStatusCode().value());
        verify(armazemService).eliminarItem(1L);
        verify(auditLogService).log(eq("ELIMINAR_ITEM_ARMAZEM"), eq("ITEM_ARMAZEM"), eq(1L), anyString());
    }

    @Test
    @DisplayName("Deve verificar stock dos itens do formulário")
    void verificarStock_DeveRetornarResultado() {
        Map<String, Map<String, Object>> mockResult = new HashMap<>();
        when(armazemService.verificarStock(List.of("HIGIENE"))).thenReturn(mockResult);

        ResponseEntity<Map<String, Map<String, Object>>> result = controller.verificarStock(List.of("HIGIENE"));

        assertEquals(200, result.getStatusCode().value());
        assertEquals(mockResult, result.getBody());
    }

    @Test
    @DisplayName("Deve verificar stock de calçado por tamanhos")
    void verificarStockCalcado_DeveRetornarResultado() {
        Map<String, Map<String, Object>> mockResult = new HashMap<>();
        when(armazemService.verificarStockCalcado(List.of("38", "39"))).thenReturn(mockResult);

        ResponseEntity<Map<String, Map<String, Object>>> result = controller.verificarStockCalcado(List.of("38", "39"));

        assertEquals(200, result.getStatusCode().value());
        assertEquals(mockResult, result.getBody());
    }

    @Test
    @DisplayName("Deve obter estatísticas de consumo por período")
    void obterEstatisticas_DeveRetornarDados() {
        ConsumoEstatisticaDTO mockStats = new ConsumoEstatisticaDTO();
        when(armazemService.obterEstatisticas("MES")).thenReturn(mockStats);

        ResponseEntity<ConsumoEstatisticaDTO> result = controller.obterEstatisticas("MES");

        assertEquals(200, result.getStatusCode().value());
        assertEquals(mockStats, result.getBody());
    }
}