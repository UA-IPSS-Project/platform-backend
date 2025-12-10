package pt.florinhas.marcacoes.domain;

public enum EventoEstado {
    AGENDADO,           // Marcação confirmada e agendada
    EM_PROGRESSO,       // Atendimento em curso
    AVISO,              // Documentos inválidos ou problemas
    CONCLUIDO,          // Atendimento finalizado
    CANCELADO,          // Marcação cancelada
    NAO_COMPARECIDO     // Utente não compareceu
}