package pt.florinhas.marcacoes.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import pt.florinhas.common_data.exception.BadRequestException;
import pt.florinhas.common_data.exception.ResourceNotFoundException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFoundException_DeveRetornar404() {

        ResponseEntity<Map<String, String>> response =
                handler.handleNotFoundException(
                        new NotFoundException("Teste"));

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Teste", response.getBody().get("message"));
    }

    @Test
    void handleBadRequestException_DeveRetornar400() {

        ResponseEntity<Map<String, String>> response =
                handler.handleBadRequestException(
                        new BadRequestException("Teste"));

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void handleIllegalArgumentException_DeveRetornar400() {

        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgumentException(
                        new IllegalArgumentException("Teste"));

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void handleBusinessRuleException_DeveRetornar400() {

        ResponseEntity<Map<String, String>> response =
                handler.handleBusinessRuleException(
                        new BusinessRuleException("Teste"));

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void handleConflictException_DeveRetornar409() {

        ResponseEntity<Map<String, String>> response =
                handler.handleConflictException(
                        new ConflictException("Teste"));

        assertEquals(409, response.getStatusCode().value());
    }

    @Test
    void handleIllegalStateException_DeveRetornar409() {

        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalStateException(
                        new IllegalStateException("Teste"));

        assertEquals(409, response.getStatusCode().value());
    }

    @Test
    void handleResourceNotFoundException_DeveRetornar404() {

        ResponseEntity<Map<String, String>> response =
                handler.handleResourceNotFoundException(
                        new ResourceNotFoundException("Teste"));

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void handleAccessDeniedException_DeveRetornar403() {

        ResponseEntity<Map<String, String>> response =
                handler.handleAccessDeniedException(
                        new AccessDeniedException("Teste"));

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void handleGenericException_DeveRetornar500() {

        ResponseEntity<Map<String, String>> response =
                handler.handleGenericException(
                        new RuntimeException("Erro"));

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Erro", response.getBody().get("message"));
    }
}