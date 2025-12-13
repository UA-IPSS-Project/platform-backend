package pt.florinhas.marcacoes.domain;

/**
 * Enumeração com a tipologia de Funcionários do sistema.
 *
 * Usada para:
 *  - definir perfis/permissões na lógica de negócio,
 *  - segmentar dashboards e métricas,
 *  - validar regras específicas por tipo (ex.: acesso a certas valências).
 */
public enum FuncionarioTipo { SECRETARIA, BALNEARIO, OUTRO, ESCOLA, INTERNOS }
