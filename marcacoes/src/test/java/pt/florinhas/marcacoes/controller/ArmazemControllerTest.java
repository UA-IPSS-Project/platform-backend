package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Mock private ArmazemService armazemService;
    @Mock private AuditLogService auditLogService;

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
    @DisplayName("Deve eliminar item e retornar No Content")
    void eliminarItem_DeveRetornarNoContent() {
        ResponseEntity<Void> result = controller.eliminarItem(1L);
        assertEquals(204, result.getStatusCode().value());
        verify(armazemService).eliminarItem(1L);
    }
}