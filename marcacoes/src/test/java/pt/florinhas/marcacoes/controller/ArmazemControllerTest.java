package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import pt.florinhas.marcacoes.dto.ConsumoEstatisticaDTO;
import pt.florinhas.marcacoes.dto.ItemArmazemDTO;
import pt.florinhas.marcacoes.service.ArmazemService;
import pt.florinhas.marcacoes.service.AuditLogService;

class ArmazemControllerTest {

    private ArmazemService service;
    private AuditLogService audit;

    private ArmazemController controller;

    @BeforeEach
    void setUp() {

        service = mock(ArmazemService.class);
        audit = mock(AuditLogService.class);

        controller =
                new ArmazemController(
                        service,
                        audit);
    }

    @Test
    void listarTodos_DeveRetornarItens() {

        when(service.listarTodos())
                .thenReturn(List.of());

        ResponseEntity<List<ItemArmazemDTO>> result =
                controller.listarTodos();

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void listarPorCategoria_DeveConverterUppercase() {

        when(service.listarPorCategoria("ROUPA"))
                .thenReturn(List.of());

        controller.listarPorCategoria("roupa");

        verify(service)
                .listarPorCategoria("ROUPA");
    }

    @Test
    void criarItem_DeveRegistarAudit() {

        ItemArmazemDTO dto =
                new ItemArmazemDTO();

        dto.setId(1L);
        dto.setNome("Sabão");
        dto.setQuantidade(10);

        when(service.criarItem(dto))
                .thenReturn(dto);

        ResponseEntity<ItemArmazemDTO> result =
                controller.criarItem(dto);

        assertEquals(200, result.getStatusCode().value());

        verify(audit)
                .log(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any());
    }

    @Test
    void eliminarItem_DeveRetornar204() {

        ResponseEntity<Void> result =
                controller.eliminarItem(1L);

        assertEquals(204, result.getStatusCode().value());

        verify(service)
                .eliminarItem(1L);
    }

    @Test
    void obterEstatisticas_DeveRetornarDto() {

        ConsumoEstatisticaDTO dto =
                mock(ConsumoEstatisticaDTO.class);

        when(service.obterEstatisticas("MES"))
                .thenReturn(dto);

        ResponseEntity<ConsumoEstatisticaDTO> result =
                controller.obterEstatisticas("MES");

        assertEquals(dto, result.getBody());
    }

    @Test
    void verificarStock_DeveRetornarResultado() {

        when(service.verificarStock(List.of("A")))
                .thenReturn(Map.of());

        ResponseEntity<Map<String, Map<String, Object>>> result =
                controller.verificarStock(List.of("A"));

        assertEquals(200, result.getStatusCode().value());
    }
}