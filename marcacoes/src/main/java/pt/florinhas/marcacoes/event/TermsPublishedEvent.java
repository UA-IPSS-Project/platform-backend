package pt.florinhas.marcacoes.event;

/**
 * Evento publicado após commit da transação de publicação de novos termos.
 * Consumido pelo @TransactionalEventListener(AFTER_COMMIT) no TermsService
 * para enviar emails apenas depois de a transação ter sido confirmada.
 */
public record TermsPublishedEvent(int newVersion, String changeDescription) {}
