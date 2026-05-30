package pt.florinhas.common_data.domain;

/**
 * Enumeração com a tipologia de Funcionários do sistema.
 *
 * Usada para:
 * - definir perfis/permissões na lógica de negócio,
 * - segmentar dashboards e métricas,
 * - validar regras específicas por tipo (ex.: acesso a certas valências).
 */
public enum FuncionarioTipo {
    SECRETARIA, BALNEARIO, OUTRO, ESCOLA, INTERNO,
    /** Responsável pela Proteção de Dados — acesso exclusivo a termos e retenção. */
    DPO,
    /** Auditor — acesso exclusivo a logs de auditoria. */
    AUDITOR
}
