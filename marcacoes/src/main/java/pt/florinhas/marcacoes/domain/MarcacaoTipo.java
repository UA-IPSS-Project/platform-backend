package pt.florinhas.marcacoes.domain;


/**
 * Enumeração que identifica a tipologia de uma marcação.
 *
 * Usos típicos:
 *  - aplicar regras de negócio distintas por tipo (fluxos, validações, SLA);
 *  - controlar permissões/visibilidade no frontend;
 *  - segmentar métricas e relatórios operacionais.
 */
public enum MarcacaoTipo { SECRETARIA, BALNEARIO }
