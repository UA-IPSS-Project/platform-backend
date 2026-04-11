package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import pt.florinhas.marcacoes.dto.ConsumoEstatisticaDTO;
import pt.florinhas.marcacoes.dto.ItemArmazemDTO;
import pt.florinhas.marcacoes.service.ArmazemService;

class ArmazemControllerTest {

    @Mock
    private ArmazemService armazemService;

    private ArmazemController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new ArmazemController(armazemService);
    }

    @Test
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
    void listarPorCategoria_DeveConverterCategoriaParaUppercase() {
        ItemArmazemDTO dto = new ItemArmazemDTO();
        dto.setCategoria("HIGIENE");

        when(armazemService.listarPorCategoria("HIGIENE")).thenReturn(List.of(dto));

        ResponseEntity<List<ItemArmazemDTO>> result = controller.listarPorCategoria("higiene");

        assertEquals(200, result.getStatusCode().value());
        verify(armazemService).listarPorCategoria("HIGIENE");
    }

    @Test
    void criarItem_DeveDelegarNoService() {
        ItemArmazemDTO input = new ItemArmazemDTO();
        input.setNome("Novo");

        ItemArmazemDTO created = new ItemArmazemDTO();
        created.setNome("Novo");

        when(armazemService.criarItem(input)).thenReturn(created);

        ResponseEntity<ItemArmazemDTO> result = controller.criarItem(input);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("Novo", result.getBody().getNome());
    }

    @Test
    void atualizarItem_DeveDelegarNoService() {
        ItemArmazemDTO input = new ItemArmazemDTO();
        input.setNome("Atualizado");

        when(armazemService.atualizarItem(1L, input)).thenReturn(input);

        ResponseEntity<ItemArmazemDTO> result = controller.atualizarItem(1L, input);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("Atualizado", result.getBody().getNome());
    }

    @Test
    void eliminarItem_DeveRetornarNoContent() {
        ResponseEntity<Void> result = controller.eliminarItem(1L);

        assertEquals(204, result.getStatusCode().value());
        verify(armazemService).eliminarItem(1L);
    }

    @Test
    void verificarStock_DeveRetornarMapa() {
        Map<String, Map<String, Object>> mapa = Map.of("Champô", Map.of("tracked", true));

        when(armazemService.verificarStock(List.of("Champô"))).thenReturn(mapa);

        ResponseEntity<Map<String, Map<String, Object>>> result = controller.verificarStock(List.of("Champô"));

        assertEquals(200, result.getStatusCode().value());
        assertEquals(true, result.getBody().get("Champô").get("tracked"));
    }

    @Test
    void verificarStockCalcado_DeveRetornarMapa() {
        Map<String, Map<String, Object>> mapa = Map.of("40", Map.of("tracked", true));

        when(armazemService.verificarStockCalcado(List.of("40"))).thenReturn(mapa);

        ResponseEntity<Map<String, Map<String, Object>>> result = controller.verificarStockCalcado(List.of("40"));

        assertEquals(200, result.getStatusCode().value());
        assertEquals(true, result.getBody().get("40").get("tracked"));
    }

    @Test
    void obterEstatisticas_DeveDelegarNoService() {
        ConsumoEstatisticaDTO dto = mock(ConsumoEstatisticaDTO.class);
        when(armazemService.obterEstatisticas("MES")).thenReturn(dto);

        ResponseEntity<ConsumoEstatisticaDTO> result = controller.obterEstatisticas("MES");

        assertEquals(200, result.getStatusCode().value());
        assertSame(dto, result.getBody());
    }
}