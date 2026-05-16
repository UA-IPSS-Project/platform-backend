package pt.florinhas.marcacoes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pt.florinhas.marcacoes.domain.EventoEstado;

/**
 * DTO para pedido de atualização do estado de uma marcação.
 *
 * Funciona como envelope vindo do frontend com:
 * - o novo estado pretendido (em string),
 * - o identificador do funcionário que efetua a operação,
 * - a versão do registo para controlo de concorrência otimista.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarEstadoRequest {

    /**
     * Novo estado pretendido, em forma textual, compatível com EventoEstado.
     * Ex.: "AGENDADO", "EM_PROGRESSO", "CONCLUIDO", ...
     * Atenção: EventoEstado.valueOf é case-sensitive e lança
     * IllegalArgumentException
     * se o valor não corresponder exatamente a um enumerado.
     */
    private String novoEstado;

    /**
     * Identificador do funcionário que executa a alteração de estado.
     * Usado para auditoria e regras de negócio (ex.: permissões).
     */
    private Long funcionarioId;

    /**
     * Versão atual do registo (da entidade Marcacao), para controlo de concorrência
     * otimista.
     * Deve ser enviada pelo cliente (e.g., obtida previamente no GET) e validada no
     * update.
     */
    private Long version;

    /**
     * Motivo do cancelamento, caso o novo estado seja CANCELADO.
     */
    private String motivoCancelamento;

    /**
     * Converte o texto de 'novoEstado' para o enum EventoEstado.
     *
     * return o valor enum correspondente
     * throws IllegalArgumentException se 'novoEstado' não corresponder a um nome
     * válido
     */
    public EventoEstado getNovoEstadoEnum() {
        return EventoEstado.valueOf(novoEstado);
    }
}