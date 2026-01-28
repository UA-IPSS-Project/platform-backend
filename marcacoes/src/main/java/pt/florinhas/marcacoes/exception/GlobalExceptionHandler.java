package pt.florinhas.marcacoes.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Tratamento global de exceções para os controladores REST.
 *
 * Esta classe centraliza o mapeamento de exceções para respostas HTTP
 * consistentes (status code + payload JSON), evitando duplicação de lógica
 * nos controllers e garantindo mensagens padronizadas para o frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Mapeia NotFoundException para HTTP 404 (NOT_FOUND).
     *
     * Corpo de resposta:
     * {
     * "message": "<detalhe do erro>"
     * }
     */
    /**
     * Mapeia NotFoundException para HTTP 404 (NOT_FOUND).
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Mapeia NoResourceFoundException (Spring 6+) para HTTP 404.
     * Evita que 404s caiam no handler genérico 500.
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResourceFoundException(
            org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "O recurso solicitado não foi encontrado: " + ex.getResourcePath());
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
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField(); // nome do campo inválido
            String errorMessage = error.getDefaultMessage(); // mensagem definida na constraint
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Erro de validação nos campos fornecidos");
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Fallback genérico para exceções não mapeadas explicitamente.
     *
     * Devolve HTTP 500 (INTERNAL_SERVER_ERROR) com uma mensagem genérica,
     * protegendo detalhes internos da aplicação em produção.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        ex.printStackTrace(); // Log no servidor para diagnóstico
        Map<String, String> error = new HashMap<>();
        // error.put("message", "Ocorreu um erro interno no servidor. Por favor, tente
        // novamente mais tarde.");
        // vv para desenvolvimento
        error.put("message", ex.getMessage() != null ? ex.getMessage() : "Ocorreu um erro interno no servidor.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
