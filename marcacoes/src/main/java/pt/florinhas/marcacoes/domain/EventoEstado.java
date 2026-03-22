package pt.florinhas.marcacoes.domain;


/**
 * Enumeração que representa o ciclo de vida/estado de uma marcação (evento).
 *
 * Estes estados suportam regras de negócio como:
 *  - transições válidas (ex.: AGENDADO -> EM_PROGRESSO -> CONCLUIDO)
 *  - deteção de exceções no fluxo (ex.: AVISO por documentos inválidos)
 *  - estados terminais (CONCLUIDO, CANCELADO, NAO_COMPARECIDO)
 */
public enum EventoEstado {
    AGENDADO,           // Marcação confirmada e agendada
    EM_PROGRESSO,       // Atendimento em curso
    AVISO,              // Documentos inválidos ou problemas
    CONCLUIDO,          // Atendimento finalizado
    CANCELADO,          // Marcação cancelada
    NAO_COMPARECIDO,    // Utente não compareceu
    EM_PREENCHIMENTO,   // Marcação em processo de criação (não finalizada)
    INVALIDO            // Marcação expirou sem ser concluída/cancelada/falta
}