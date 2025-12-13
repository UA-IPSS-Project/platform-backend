package pt.florinhas.marcacoes.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de uma marcação (secretaria/remota).
 *
 * Transporta do frontend:
 *  - o instante pretendido para a marcação,
 *  - metadados descritivos (assunto/descrição),
 *  - identificadores de utente/funcionário envolvidos,
 *  - e, opcionalmente, dados mínimos para criação automática de um novo utente
 *    caso o utente ainda não exista no sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarMarcacaoRequest {

    // Instante desejado para a marcação (data/hora local). 
    private LocalDateTime data;

    // Assunto curto da marcação (título). 
    private String assunto;

    // Descrição detalhada do pedido/atendimento. 
    private String descricao;

    // ID do utente associado à marcação (se já existir). 
    private Long utenteId;

    // ID do funcionário alocado/associado (se aplicável). 
    private Long funcionarioId;

    /**
     * ID do utilizador que cria o registo (ex.: funcionário de secretaria ou o próprio utente).
     * Útil para auditoria e regras de autorização.
     */
    private Long criadoPorId;

    // ===== Dados opcionais para criação “on-the-fly” de um novo utente =====

    // NIF do novo utente (se não existir ainda no sistema).
    private String utenteNif;

    // Nome do novo utente.
    private String utenteNome;

    // Email do novo utente. 
    private String utenteEmail;

    // Telefone do novo utente. 
    private String utenteTelefone;
}
