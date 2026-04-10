package pt.florinhas.common_data.exception;
/**
 * Exceção de domínio para sinalizar erros de validação/negócio
 * que correspondem a um HTTP 400 (Bad Request) ao nível da API.
 *
 * Usos típicos:
 *  - Falhas de validação de input (campos obrigatórios, formatos inválidos).
 *  - Quebra de invariantes de negócio (ex.: intervalo horário inválido).
 */
public class BadRequestException extends RuntimeException {
    /**
     * Cria uma BadRequestException com mensagem descritiva.
     * param message descrição do erro a apresentar/logar
     */
    public BadRequestException(String message) {
        super(message);
    }
    /**
     * Cria uma BadRequestException com mensagem e causa encadeada.
     * Útil para preservar a stacktrace original de erros subjacentes.
     *
     * param message descrição do erro a apresentar/logar
     * param cause exceção original que originou este erro
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
