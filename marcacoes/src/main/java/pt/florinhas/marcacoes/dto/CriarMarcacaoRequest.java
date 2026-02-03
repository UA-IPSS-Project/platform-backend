package pt.florinhas.marcacoes.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de uma marcação (secretaria/remota).
 *
 * Transporta do frontend:
 * - o instante pretendido para a marcação,
 * - metadados descritivos (assunto/descrição),
 * - identificadores de utente/funcionário envolvidos,
 * - e, opcionalmente, dados mínimos para criação automática de um novo utente
 * caso o utente ainda não exista no sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarMarcacaoRequest {

    // Instante desejado para a marcação (data/hora local).
    @NotNull(message = "A data da marcação é obrigatória")
    private LocalDateTime data;

    // Assunto curto da marcação (título).
    @Size(max = 200, message = "O assunto não pode exceder 200 caracteres")
    private String assunto;

    // Descrição detalhada do pedido/atendimento.
    @Size(max = 2000, message = "A descrição não pode exceder 2000 caracteres")
    private String descricao;

    // ID do utente associado à marcação (se já existir).
    private Long utenteId;

    // ID do funcionário alocado/associado (se aplicável).
    private Long funcionarioId;

    /**
     * ID do utilizador que cria o registo (ex.: funcionário de secretaria ou o
     * próprio utente).
     * Útil para auditoria e regras de autorização.
     */
    private Long criadoPorId;

    // ===== Dados opcionais para criação “on-the-fly” de um novo utente =====

    // NIF do novo utente (se não existir ainda no sistema).
    @Pattern(regexp = "^$|^[0-9]{9}$", message = "NIF deve conter 9 dígitos")
    private String utenteNif;

    // Nome do novo utente.
    @Size(max = 100, message = "O nome não pode exceder 100 caracteres")
    private String utenteNome;

    // Email do novo utente.
    @Email(message = "Email inválido")
    private String utenteEmail;

    // Telefone do novo utente.
    @Pattern(regexp = "^$|^[0-9+\\s-]{9,20}$", message = "Telefone inválido")
    private String utenteTelefone;
}
