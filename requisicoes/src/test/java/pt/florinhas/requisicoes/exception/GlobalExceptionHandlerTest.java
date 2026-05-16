package pt.florinhas.requisicoes.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }
    @Test
    void handleResourceNotFound_DeveRetornar404() {

        ResourceNotFoundException ex =
                new ResourceNotFoundException("Not found");

        ResponseEntity<Map<String, Object>> response =
                handler.handleResourceNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Not found", response.getBody().get("message"));
    }

    @Test
    void handleIllegalArgument_DeveRetornar400() {

        IllegalArgumentException ex =
                new IllegalArgumentException("Invalid");

        ResponseEntity<Map<String, Object>> response =
                handler.handleIllegalArgument(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid", response.getBody().get("message"));
    }

    @Test
    void handleUnexpected_DeveRetornar500() {

        Exception ex = new Exception("Erro");

        ResponseEntity<Map<String, Object>> response =
                handler.handleUnexpected(ex);

        assertEquals(500, response.getStatusCode().value());
        assertEquals(
                "Erro interno no servidor.",
                response.getBody().get("message"));
    }
}