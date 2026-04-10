package pt.florinhas.marcacoes.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpMethod;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
    }

    // =========================
    // NOT FOUND
    // =========================

    @Test
    void handleNotFoundException_DeveRetornar404() {
        NotFoundException ex = new NotFoundException("Não encontrado");

        ResponseEntity<Map<String, String>> result =
                handler.handleNotFoundException(ex);

        assertEquals(404, result.getStatusCode().value());
        assertEquals("Não encontrado", result.getBody().get("message"));
    }

    @Test
    void handleResourceNotFoundException_DeveRetornar404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso não existe");

        ResponseEntity<Map<String, String>> result =
                handler.handleResourceNotFoundException(ex);

        assertEquals(404, result.getStatusCode().value());
        assertEquals("Recurso não existe", result.getBody().get("message"));
    }

    @Test
void handleNoResourceFoundException_DeveRetornar404() {
    NoResourceFoundException ex =
            new NoResourceFoundException(HttpMethod.GET, "/api/teste");

    ResponseEntity<Map<String, String>> result =
            handler.handleNoResourceFoundException(ex);

    assertEquals(404, result.getStatusCode().value());
    assertTrue(result.getBody().get("message").contains("/api/teste"));
}

    // =========================
    // BAD REQUEST
    // =========================

    @Test
    void handleBadRequestException_DeveRetornar400() {
        BadRequestException ex = new BadRequestException("Erro de validação");

        ResponseEntity<Map<String, String>> result =
                handler.handleBadRequestException(ex);

        assertEquals(400, result.getStatusCode().value());
        assertEquals("Erro de validação", result.getBody().get("message"));
    }

    @Test
    void handleIllegalArgumentException_DeveRetornar400() {
        IllegalArgumentException ex = new IllegalArgumentException("Argumento inválido");

        ResponseEntity<Map<String, String>> result =
                handler.handleIllegalArgumentException(ex);

        assertEquals(400, result.getStatusCode().value());
        assertEquals("Argumento inválido", result.getBody().get("message"));
    }

    @Test
    void handleBusinessRuleException_DeveRetornar400() {
        BusinessRuleException ex = new BusinessRuleException("Regra violada");

        ResponseEntity<Map<String, String>> result =
                handler.handleBusinessRuleException(ex);

        assertEquals(400, result.getStatusCode().value());
        assertEquals("Regra violada", result.getBody().get("message"));
    }

    // =========================
    // CONFLICT
    // =========================

    @Test
    void handleConflictException_DeveRetornar409() {
        ConflictException ex = new ConflictException("Conflito");

        ResponseEntity<Map<String, String>> result =
                handler.handleConflictException(ex);

        assertEquals(409, result.getStatusCode().value());
        assertEquals("Conflito", result.getBody().get("message"));
    }

    @Test
    void handleIllegalStateException_DeveRetornar409() {
        IllegalStateException ex = new IllegalStateException("Estado inválido");

        ResponseEntity<Map<String, String>> result =
                handler.handleIllegalStateException(ex);

        assertEquals(409, result.getStatusCode().value());
        assertEquals("Estado inválido", result.getBody().get("message"));
    }

    // =========================
    // VALIDATION (@Valid)
    // =========================

    @Test
    void handleValidationExceptions_DeveRetornar400ComMapaDeErros() {
        // Simular erro de validação
        Object target = new Object();
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(target, "obj");

        bindingResult.addError(new FieldError("obj", "nome", "Nome obrigatório"));
        bindingResult.addError(new FieldError("obj", "email", "Email inválido"));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> result =
                handler.handleValidationExceptions(ex);

        assertEquals(400, result.getStatusCode().value());

        Map<String, Object> body = result.getBody();

        assertEquals("Nome obrigatório", body.get("message"));

        Map<String, String> errors = (Map<String, String>) body.get("errors");
        assertEquals("Nome obrigatório", errors.get("nome"));
        assertEquals("Email inválido", errors.get("email"));
    }

    // =========================
    // ACCESS DENIED
    // =========================

    @Test
    void handleAccessDeniedException_DeveRetornar403() {
        AccessDeniedException ex = new AccessDeniedException("Acesso negado");

        ResponseEntity<Map<String, String>> result =
                handler.handleAccessDeniedException(ex);

        assertEquals(403, result.getStatusCode().value());
        assertEquals("Acesso negado", result.getBody().get("message"));
    }

    // =========================
    // GENERIC
    // =========================

    @Test
    void handleGenericException_ComMensagem_DeveRetornar500() {
        Exception ex = new Exception("Erro interno");

        ResponseEntity<Map<String, String>> result =
                handler.handleGenericException(ex);

        assertEquals(500, result.getStatusCode().value());
        assertEquals("Erro interno", result.getBody().get("message"));
    }

    @Test
    void handleGenericException_SemMensagem_DeveRetornarMensagemDefault() {
        Exception ex = new Exception((String) null);

        ResponseEntity<Map<String, String>> result =
                handler.handleGenericException(ex);

        assertEquals(500, result.getStatusCode().value());
        assertEquals("Ocorreu um erro interno no servidor.", result.getBody().get("message"));
    }
}