package pt.florinhas.marcacoes.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpMethod;

import pt.florinhas.common_data.exception.BadRequestException;
import pt.florinhas.common_data.exception.ResourceNotFoundException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {

        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleNotFoundException deve devolver 404")
    void handleNotFoundException_DeveDevolver404() {

        NotFoundException ex =
                new NotFoundException("Não encontrado");

        ResponseEntity<Map<String, String>> response =
                handler.handleNotFoundException(ex);

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertEquals(
                "Não encontrado",
                response.getBody().get("message")
        );
    }

    @Test
    @DisplayName("handleBadRequestException deve devolver 400")
    void handleBadRequestException_DeveDevolver400() {

        BadRequestException ex =
                new BadRequestException("Pedido inválido");

        ResponseEntity<Map<String, String>> response =
                handler.handleBadRequestException(ex);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );
    }

    @Test
    @DisplayName("handleIllegalArgumentException deve devolver 400")
    void handleIllegalArgumentException_DeveDevolver400() {

        IllegalArgumentException ex =
                new IllegalArgumentException("Argumento inválido");

        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgumentException(ex);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );
    }

    @Test
    @DisplayName("handleBusinessRuleException deve devolver 400")
    void handleBusinessRuleException_DeveDevolver400() {

        BusinessRuleException ex =
                new BusinessRuleException("Regra inválida");

        ResponseEntity<Map<String, String>> response =
                handler.handleBusinessRuleException(ex);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );
    }

    @Test
    @DisplayName("handleConflictException deve devolver 409")
    void handleConflictException_DeveDevolver409() {

        ConflictException ex =
                new ConflictException("Conflito");

        ResponseEntity<Map<String, String>> response =
                handler.handleConflictException(ex);

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );
    }

    @Test
    @DisplayName("handleIllegalStateException deve devolver 409")
    void handleIllegalStateException_DeveDevolver409() {

        IllegalStateException ex =
                new IllegalStateException("Estado inválido");

        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalStateException(ex);

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );
    }

    @Test
    @DisplayName("handleResourceNotFoundException deve devolver 404")
    void handleResourceNotFoundException_DeveDevolver404() {

        ResourceNotFoundException ex =
                new ResourceNotFoundException("Recurso");

        ResponseEntity<Map<String, String>> response =
                handler.handleResourceNotFoundException(ex);

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );
    }

    @Test
    @DisplayName("handleAccessDeniedException deve devolver 403")
    void handleAccessDeniedException_DeveDevolver403() {

        AccessDeniedException ex =
                new AccessDeniedException("Acesso negado");

        ResponseEntity<Map<String, String>> response =
                handler.handleAccessDeniedException(ex);

        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode()
        );
    }

    @Test
    @DisplayName("handleGenericException deve devolver 500")
    void handleGenericException_DeveDevolver500() {

        Exception ex =
                new Exception("Erro interno");

        ResponseEntity<Map<String, String>> response =
                handler.handleGenericException(ex);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertEquals(
                "Erro interno",
                response.getBody().get("message")
        );
    }

    @Test
    @DisplayName("handleNoResourceFoundException deve devolver 404")
    void handleNoResourceFoundException_DeveDevolver404() {

        NoResourceFoundException ex =
                new NoResourceFoundException(
                        HttpMethod.GET,
                        "/teste"
                );

        ResponseEntity<Map<String, String>> response =
                handler.handleNoResourceFoundException(ex);

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );
    }

    @Test
    @DisplayName("handleValidationExceptions deve devolver 400")
    void handleValidationExceptions_DeveDevolver400() {

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "obj");

        bindingResult.addError(
                new FieldError(
                        "obj",
                        "nome",
                        "Nome obrigatório"
                )
        );

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(
                        (MethodParameter) null,
                        bindingResult
                );

        ResponseEntity<Map<String, Object>> response =
                handler.handleValidationExceptions(ex);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Nome obrigatório",
                response.getBody().get("message")
        );
    }
}