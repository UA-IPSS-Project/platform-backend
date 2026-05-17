package pt.florinhas.marcacoes.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

import pt.florinhas.common_data.exception.BadRequestException;
import pt.florinhas.common_data.exception.ResourceNotFoundException;

/**
 * Tratamento global de exceções para os controladores REST.
 *
 * Esta classe centraliza o mapeamento de exceções para respostas HTTP
 * consistentes (status code + payload JSON), evitando duplicação de lógica
 * nos controllers e garantindo mensagens padronizadas para o frontend.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String KEY_MESSAGE = "message";

    /**
     * Mapeia NotFoundException para HTTP 404 (NOT_FOUND).
     *
     * Corpo de resposta:
     * {
     * "message": "<detalhe do erro>"
     * }
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put(KEY_MESSAGE, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Mapeia NoResourceFoundException (Spring 6+) para HTTP 404.
     * Evita que 404s caiam no handler genérico 500.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResourceFoundException(
            NoResourceFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put(KEY_MESSAGE, "O recurso solicitado não foi encontrado: " + ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Mapeia BadRequestException para HTTP 400 (BAD_REQUEST).
     *
     * Corpo de resposta:
     * {
     * "message": "<detalhe do erro>"
     * }
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequestException(BadRequestException ex) {
        Map<String, String> error = new HashMap<>();
        error.put(KEY_MESSAGE, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Mapeia IllegalArgumentException para HTTP 400 (BAD_REQUEST).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put(KEY_MESSAGE, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Mapeia BusinessRuleException para HTTP 400 (BAD_REQUEST) ou 422
     * (UNPROCESSABLE_ENTITY).
     * Optou-se por 400 para manter consistência, mas semanticamente 422 seria
     * adequado.
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, String>> handleBusinessRuleException(BusinessRuleException ex) {
        Map<String, String> error = new HashMap<>();
        error.put(KEY_MESSAGE, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Mapeia ConflictException para HTTP 409 (CONFLICT).
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflictException(ConflictException ex) {
        Map<String, String> error = new HashMap<>();
        error.put(KEY_MESSAGE, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Mapeia IllegalStateException para HTTP 409 (CONFLICT).
     * Usado, por exemplo, quando um slot está cheio durante reagendamento.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        Map<String, String> error = new HashMap<>();
        error.put(KEY_MESSAGE, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Mapeia ResourceNotFoundException para HTTP 404 (NOT_FOUND).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put(KEY_MESSAGE, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Mapeia erros de validação Bean Validation (@Valid) para HTTP 400.
     *
     * Agrega os erros por campo e devolve um payload estruturado:
     * {
     * "message": "Erro de validação nos campos fornecidos",
     * "errors": {
     * "campo1": "mensagem de erro",
     * "campo2": "mensagem de erro"
     * }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField(); // nome do campo inválido
            String errorMessage = error.getDefaultMessage(); // mensagem definida na constraint
            errors.put(fieldName, errorMessage);
        });

        String mainMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Erro de validação nos campos fornecidos");

        Map<String, Object> response = new HashMap<>();
        response.put(KEY_MESSAGE, mainMessage);
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Mapeia AccessDeniedException para HTTP 403 (FORBIDDEN).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(
            AccessDeniedException ex) {
        Map<String, String> error = new HashMap<>();
        error.put(KEY_MESSAGE, ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Fallback genérico para exceções não mapeadas explicitamente.
     *
     * Devolve HTTP 500 (INTERNAL_SERVER_ERROR) com uma mensagem genérica,
     * protegendo detalhes internos da aplicação em produção.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        Map<String, String> error = new HashMap<>();

        error.put(KEY_MESSAGE, ex.getMessage() != null ? ex.getMessage() : "Ocorreu um erro interno no servidor.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}