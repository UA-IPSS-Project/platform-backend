package pt.florinhas.marcacoes.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import pt.florinhas.common_data.exception.BadRequestException;
import pt.florinhas.common_data.exception.ResourceNotFoundException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Deve retornar 404 para NotFoundException")
    void handleNotFoundException_DeveDevolver404() {
        NotFoundException ex = new NotFoundException("Não encontrado");
        ResponseEntity<Map<String, String>> response = handler.handleNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Não encontrado", response.getBody().get("message"));
    }

    @Test
    @DisplayName("Deve retornar 400 para BadRequestException")
    void handleBadRequestException_DeveDevolver400() {
        BadRequestException ex = new BadRequestException("Pedido inválido");
        ResponseEntity<Map<String, String>> response = handler.handleBadRequestException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar 403 para AccessDeniedException")
    void handleAccessDeniedException_DeveDevolver403() {
        AccessDeniedException ex = new AccessDeniedException("Acesso negado");
        ResponseEntity<Map<String, String>> response = handler.handleAccessDeniedException(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar 500 para exceção genérica")
    void handleGenericException_DeveDevolver500() {
        Exception ex = new Exception("Erro interno");
        ResponseEntity<Map<String, String>> response = handler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Erro interno", response.getBody().get("message"));
    }

    @Test
    @DisplayName("Deve retornar 409 para ConflictException")
    void handleConflictException_DeveDevolver409() {
        ConflictException ex = new ConflictException("Conflito");
        ResponseEntity<Map<String, String>> response = handler.handleConflictException(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflito", response.getBody().get("message"));
    }

    @Test
    @DisplayName("Deve retornar 404 para ResourceNotFoundException")
    void handleResourceNotFoundException_DeveDevolver404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso não encontrado");
        ResponseEntity<Map<String, String>> response = handler.handleResourceNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Recurso não encontrado", response.getBody().get("message"));
    }

    @Test
    @DisplayName("Deve retornar 400 para erros de validação")
    void handleValidationExceptions_DeveDevolver400() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "nome", "Nome obrigatório"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mock(MethodParameter.class),
                bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Nome obrigatório", response.getBody().get("message"));
    }
}