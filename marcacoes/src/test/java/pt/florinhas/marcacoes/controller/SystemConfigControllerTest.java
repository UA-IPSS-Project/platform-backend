package pt.florinhas.marcacoes.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import pt.florinhas.marcacoes.service.SystemConfigService;

class SystemConfigControllerTest {

    private SystemConfigService service;
    private SystemConfigController controller;

    @BeforeEach
    void setUp() {

        service = mock(SystemConfigService.class);

        controller = new SystemConfigController(service);
    }

    @Test
    void getRetencaoDocumentos_DeveRetornarValor() {

        when(service.getConfigValueAsInt(
                "documento.retencao.anos",
                5))
                .thenReturn(10);

        ResponseEntity<Map<String, Object>> result =
                controller.getRetencaoDocumentos();

        assertEquals(
                10,
                result.getBody().get("anos"));
    }

    @Test
    void setRetencaoDocumentos_DeveFalhar() {

        ResponseEntity<Map<String, Object>> result =
                controller.setRetencaoDocumentos(
                        Map.of("anos", 0));

        assertEquals(400,
                result.getStatusCode().value());
    }

    @Test
    void setRetencaoDocumentos_DeveAtualizar() {

        ResponseEntity<Map<String, Object>> result =
                controller.setRetencaoDocumentos(
                        Map.of("anos", 10));

        assertEquals(200,
                result.getStatusCode().value());

        verify(service)
                .setConfigValue(
                        "documento.retencao.anos",
                        "10",
                        "Prazo de retenção de documentos em anos (RGPD)");
    }
}