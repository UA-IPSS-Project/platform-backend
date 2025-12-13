package pt.florinhas.marcacoes.exception;

/**
 * Exceção de domínio para sinalizar recursos não encontrados,
 * devendo ser mapeada para HTTP 404 (Not Found) na camada web.
 *
 * Usos típicos:
 *  - Entidade não existente para um dado ID/NIF/email.
 *  - Consulta por critérios que não devolve resultados obrigatórios.
 */
public class NotFoundException extends RuntimeException {
    
    /**
     * Cria uma NotFoundException com mensagem descritiva.
     *
     * param message descrição do erro a apresentar/logar
     */
    public NotFoundException(String message) {
        super(message);
    }
    
    /**
     * Cria uma NotFoundException com mensagem e causa encadeada.
     * Útil para preservar a stacktrace original de erros subjacentes.
     *
     * param message descrição do erro a apresentar/logar
     * param cause exceção original que originou este erro
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
