package pt.florinhas.marcacoes.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;

import pt.florinhas.common_data.domain.Utente;
import pt.florinhas.common_data.domain.Utilizador;

/**
 * Repositório Spring Data JPA para a entidade Marcacao.
 *
 * Responsabilidades:
 * - Operações CRUD herdadas de JpaRepository.
 * - Consultas derivadas por convenção (findBy..., deleteBy...).
 * - Consultas JPQL customizadas para pesquisa por utente, criador, intervalo
 * temporal,
 * estado, estatísticas e limpeza de reservas temporárias.
 */
@Repository
public interface MarcacaoRepository extends JpaRepository<Marcacao, Long> {

        // Encontrar marcações por utente — com fetch de todas as relações acedidas no toDTO
        @EntityGraph(attributePaths = {
                "atendente",
                "marcacaoSecretaria", "marcacaoSecretaria.utente",
                "marcacaoBalneario", "marcacaoBalneario.responsavel",
                "marcacaoBalneario.roupas", "marcacaoBalneario.roupas.item"
        })
        @Query("SELECT m FROM Marcacao m WHERE m.marcacaoSecretaria.utente = :utente")
        List<Marcacao> findByUtente(@Param("utente") Utente utente);

        // Encontrar marcações criadas por utilizador — com fetch de todas as relações acedidas no toDTO
        @EntityGraph(attributePaths = {
                "atendente",
                "marcacaoSecretaria", "marcacaoSecretaria.utente",
                "marcacaoBalneario", "marcacaoBalneario.responsavel",
                "marcacaoBalneario.roupas", "marcacaoBalneario.roupas.item"
        })
        @Query("SELECT m FROM Marcacao m WHERE m.criadoPor = :criadoPor")
        List<Marcacao> findByCriadoPor(@Param("criadoPor") Utilizador criadoPor);

        // Carregar entidades por IDs com fetch completo — usado após paginação de IDs nas passadas
        @EntityGraph(attributePaths = {
                "atendente",
                "marcacaoSecretaria", "marcacaoSecretaria.utente",
                "marcacaoBalneario", "marcacaoBalneario.responsavel",
                "marcacaoBalneario.roupas", "marcacaoBalneario.roupas.item"
        })
        @Query("SELECT m FROM Marcacao m WHERE m.id IN :ids")
        List<Marcacao> findAllByIdWithDetails(@Param("ids") List<Long> ids);

        // Encontrar marcações por estado
        List<Marcacao> findByEstado(EventoEstado estado);

        // Encontrar marcações entre datas com JOIN FETCH para evitar N+1
        @Query("SELECT DISTINCT m FROM Marcacao m " +
                        "LEFT JOIN FETCH m.marcacaoSecretaria ms " +
                        "LEFT JOIN FETCH ms.utente u " +
                        "LEFT JOIN FETCH m.criadoPor cp " +
                        "LEFT JOIN FETCH m.atendente " +
                        "LEFT JOIN FETCH m.marcacaoBalneario mb " +
                        "LEFT JOIN FETCH mb.responsavel " +
                        "LEFT JOIN FETCH mb.roupas roupas " +
                        "LEFT JOIN FETCH roupas.item " +
                        "WHERE m.data >= :dataInicio AND m.data <= :dataFim " +
                        "AND (:tipo IS NULL OR " +
                        "    (:tipo = 'BALNEARIO' AND m.marcacaoBalneario IS NOT NULL) OR " +
                        "    (:tipo = 'SECRETARIA' AND m.marcacaoSecretaria IS NOT NULL)) " +
                        "ORDER BY m.data ASC")
        List<Marcacao> findMarcacoesBetweenDates(
                        @Param("dataInicio") LocalDateTime dataInicio,
                        @Param("dataFim") LocalDateTime dataFim,
                        @Param("tipo") String tipo);

        @Query("SELECT DISTINCT m FROM Marcacao m " +
                        "LEFT JOIN FETCH m.marcacaoSecretaria ms " +
                        "LEFT JOIN FETCH ms.utente u " +
                        "LEFT JOIN FETCH m.criadoPor cp " +
                        "LEFT JOIN FETCH m.atendente " +
                        "LEFT JOIN FETCH m.marcacaoBalneario mb " +
                        "LEFT JOIN FETCH mb.responsavel " +
                        "LEFT JOIN FETCH mb.roupas roupas " +
                        "LEFT JOIN FETCH roupas.item " +
                        "WHERE m.data > :now AND m.estado IN ('AGENDADO', 'AVISO') " +
                        "AND (:tipo IS NULL OR " +
                        "     (:tipo = 'BALNEARIO' AND m.marcacaoBalneario IS NOT NULL) OR " +
                        "     (:tipo = 'SECRETARIA' AND m.marcacaoSecretaria IS NOT NULL)) " +
                        "ORDER BY m.data ASC, m.id ASC")
        List<Marcacao> findActiveFutureMarcacoes(
                        @Param("now") LocalDateTime now,
                        @Param("tipo") String tipo);

        // Verificar se existe marcação no mesmo horário exato (não cancelada)
        @Query("SELECT COUNT(m) > 0 FROM Marcacao m WHERE m.data = :data AND m.estado <> :estado")
        boolean existsByDataAndEstadoNot(
                        @Param("data") LocalDateTime data,
                        @Param("estado") EventoEstado estado);

        @Query("SELECT COUNT(m) FROM Marcacao m WHERE m.data = :data " +
                        "AND m.estado <> 'CANCELADO' " +
                        "AND (:tipo IS NULL OR (:tipo = 'BALNEARIO' AND m.marcacaoBalneario IS NOT NULL) OR (:tipo = 'SECRETARIA' AND m.marcacaoSecretaria IS NOT NULL))")
        long countByDataAndTipo(@Param("data") LocalDateTime data, @Param("tipo") String tipo);

        /**
         * Conta o número de marcações para uma determinada data/hora e lista de
         * estados.
         * Útil para verificar vagas disponíveis num horário com capacidade > 1.
         */
        long countByDataAndEstadoIn(LocalDateTime data, List<EventoEstado> estados);

        /**
         * Conta o número de marcações para uma determinada data/hora, lista de estados
         * e tipo (SECRETARIA/BALNEARIO).
         * Permite distinguir entre marcações da secretaria e do balneário, ou ambos
         * (tipo = null).
         */
        @Query("SELECT COUNT(m) FROM Marcacao m WHERE m.data = :data AND m.estado IN :estados " +
                        "AND (:tipo IS NULL OR (:tipo = 'BALNEARIO' AND m.marcacaoBalneario IS NOT NULL) OR (:tipo = 'SECRETARIA' AND m.marcacaoSecretaria IS NOT NULL))")
        long countByDataAndEstadoInAndTipo(@Param("data") LocalDateTime data,
                        @Param("estados") List<EventoEstado> estados, @Param("tipo") String tipo);

        // Consulta com Lock Pessimista para evitar Race Conditions
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT m FROM Marcacao m WHERE m.data = :data AND m.estado <> 'CANCELADO'")
        List<Marcacao> findConflictingWithLock(@Param("data") LocalDateTime data);

        // Consulta com filtros múltiplos para agenda (Optimized)
        @Query("SELECT DISTINCT m FROM Marcacao m " +
                        "LEFT JOIN FETCH m.marcacaoSecretaria ms " +
                        "LEFT JOIN FETCH ms.utente u " +
                        "LEFT JOIN FETCH m.criadoPor cp " +
                        "LEFT JOIN FETCH m.atendente " +
                        "LEFT JOIN FETCH m.marcacaoBalneario mb " +
                        "LEFT JOIN FETCH mb.responsavel " +
                        "LEFT JOIN FETCH mb.roupas roupas " +
                        "LEFT JOIN FETCH roupas.item " +
                        "WHERE " +
                        "(:dataInicio IS NULL OR m.data >= :dataInicio) AND " +
                        "(:dataFim IS NULL OR m.data <= :dataFim) AND " +
                        "(:criadoPorId IS NULL OR m.criadoPor.id = :criadoPorId) AND " +
                        "(:utenteId IS NULL OR ms.utente.id = :utenteId) AND " +
                        "(:estado IS NULL OR m.estado = :estado) " +
                        "ORDER BY m.data")
        List<Marcacao> findWithFilters(
                        @Param("dataInicio") LocalDateTime dataInicio,
                        @Param("dataFim") LocalDateTime dataFim,
                        @Param("criadoPorId") Long criadoPorId,
                        @Param("utenteId") Long utenteId,
                        @Param("estado") EventoEstado estado);

        // Consultar marcações passadas com filtros (Optimized)
        @Query("SELECT DISTINCT m FROM Marcacao m " +
                        "LEFT JOIN FETCH m.marcacaoSecretaria ms " +
                        "LEFT JOIN FETCH ms.utente u " +
                        "LEFT JOIN FETCH m.criadoPor cp " +
                        "LEFT JOIN FETCH m.atendente " +
                        "LEFT JOIN FETCH m.marcacaoBalneario mb " +
                        "LEFT JOIN FETCH mb.responsavel " +
                        "LEFT JOIN FETCH mb.roupas roupas " +
                        "LEFT JOIN FETCH roupas.item " +
                        "WHERE " +
                        "m.estado IN ('CONCLUIDO', 'NAO_COMPARECIDO', 'CANCELADO') AND " +
                        "m.data >= :dataInicio AND " +
                        "m.data <= :dataFim AND " +
                        "(:utenteId IS NULL OR m.marcacaoSecretaria.utente.id = :utenteId) AND " +
                        "(:estado IS NULL OR m.estado = :estado) " +
                        "ORDER BY m.data DESC")
        List<Marcacao> findMarcacoesPassadas(
                        @Param("dataInicio") LocalDateTime dataInicio,
                        @Param("dataFim") LocalDateTime dataFim,
                        @Param("utenteId") Long utenteId,
                        @Param("estado") EventoEstado estado);

        @Query(value = """
                        SELECT m.id FROM marcacao m
                        LEFT JOIN marcacao_secretaria ms ON m.id = ms.marcacao_id
                        LEFT JOIN utente u ON u.id = ms.utente_id
                        LEFT JOIN utilizador ul ON ul.id = u.id
                        WHERE m.estado IN ('CONCLUIDO', 'NAO_COMPARECIDO', 'CANCELADO')
                        AND m.data >= :dataInicio AND m.data <= :dataFim
                        AND (:utenteId IS NULL OR u.id = :utenteId)
                        AND (:estado IS NULL OR m.estado = CAST(:estado AS VARCHAR))
                        AND (:assunto IS NULL OR ms.assunto ILIKE ('%' || CAST(:assunto AS TEXT) || '%'))
                        AND (:nomeUtente IS NULL OR ul.nome ILIKE ('%' || CAST(:nomeUtente AS TEXT) || '%'))
                        ORDER BY m.data DESC
                        """,
                        countQuery = """
                        SELECT COUNT(m.id) FROM marcacao m
                        LEFT JOIN marcacao_secretaria ms ON m.id = ms.marcacao_id
                        LEFT JOIN utente u ON u.id = ms.utente_id
                        LEFT JOIN utilizador ul ON ul.id = u.id
                        WHERE m.estado IN ('CONCLUIDO', 'NAO_COMPARECIDO', 'CANCELADO')
                        AND m.data >= :dataInicio AND m.data <= :dataFim
                        AND (:utenteId IS NULL OR u.id = :utenteId)
                        AND (:estado IS NULL OR m.estado = CAST(:estado AS VARCHAR))
                        AND (:assunto IS NULL OR ms.assunto ILIKE ('%' || CAST(:assunto AS TEXT) || '%'))
                        AND (:nomeUtente IS NULL OR ul.nome ILIKE ('%' || CAST(:nomeUtente AS TEXT) || '%'))
                        """,
                        nativeQuery = true)
        Page<Long> findMarcacoesPassadasPaginatedIds(
                        @Param("dataInicio") LocalDateTime dataInicio,
                        @Param("dataFim") LocalDateTime dataFim,
                        @Param("utenteId") Long utenteId,
                        @Param("estado") String estado,
                        @Param("assunto") String assunto,
                        @Param("nomeUtente") String nomeUtente,
                        Pageable pageable);

        // Estatísticas - contar marcações por estado
        @Query("SELECT m.estado, COUNT(m) FROM Marcacao m GROUP BY m.estado")
        List<Object[]> countMarcacoesByEstado();

        // Contar marcações entre datas específicas
        @Query("SELECT COUNT(m) FROM Marcacao m WHERE m.data BETWEEN :dataInicio AND :dataFim AND m.estado <> 'CANCELADO'")
        long countMarcacoesBetweenDates(
                        @Param("dataInicio") LocalDateTime dataInicio,
                        @Param("dataFim") LocalDateTime dataFim);

        /**
         * Lista marcações com data no intervalo indicado.
         * Método derivado pelo nome (equivalente a BETWEEN).
         */
        List<Marcacao> findByDataBetween(LocalDateTime inicioBloqueio, LocalDateTime fimBloqueio);

        /**
         * Remove marcações com determinado estado criadas antes de um limite temporal.
         * Usado para limpeza de reservas temporárias (ex.: EM_PREENCHIMENTO expiradas).
         */
        List<Marcacao> findByEstadoAndCriadoEmBefore(EventoEstado estado, LocalDateTime limite);

        @Modifying(clearAutomatically = true)
        @Query("UPDATE Marcacao m SET m.estado = :novoEstado, m.version = m.version + 1 WHERE m.estado NOT IN :estadosExcluidos AND m.data < :limite")
        int invalidarMarcacoesAntigas(
                        @Param("novoEstado") EventoEstado novoEstado,
                        @Param("estadosExcluidos") List<EventoEstado> estadosExcluidos,
                        @Param("limite") LocalDateTime limite);

        @Modifying(clearAutomatically = true)
        @Query("UPDATE Marcacao m SET m.estado = :novoEstado, m.version = m.version + 1 WHERE m.estado = :estadoAtual AND m.data < :limite")
        int atualizarMarcacoesPorEstadoAntigas(
                        @Param("novoEstado") EventoEstado novoEstado,
                        @Param("estadoAtual") EventoEstado estadoAtual,
                        @Param("limite") LocalDateTime limite);

        // Consulta paginada com fetch para evitar N+1
        @Query(value = "SELECT DISTINCT m FROM Marcacao m " +
                        "LEFT JOIN FETCH m.marcacaoSecretaria ms " +
                        "LEFT JOIN FETCH ms.utente u " +
                        "LEFT JOIN FETCH m.criadoPor cp", countQuery = "SELECT COUNT(m) FROM Marcacao m")
        Page<Marcacao> findAllWithRelations(Pageable pageable);

        // Step 1 do 2-step pagination para listarTodas — retorna IDs sem JOIN FETCH
        @Query(value = "SELECT m.id FROM Marcacao m ORDER BY m.data DESC",
               countQuery = "SELECT COUNT(m) FROM Marcacao m")
        Page<Long> findAllIdsPaginated(Pageable pageable);
    @Query("SELECT COUNT(m) FROM Marcacao m WHERE m.marcacaoBalneario IS NOT NULL " +
            "AND m.estado IN ('EM_PROGRESSO', 'CONCLUIDO') " +
            "AND m.data BETWEEN :inicio AND :fim")
    long countBalnearioAttendance(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(m) FROM Marcacao m WHERE m.marcacaoBalneario IS NOT NULL " +
            "AND m.estado != 'CANCELADO' " +
            "AND m.data BETWEEN :inicio AND :fim")
    long countTotalBalnearioAttendance(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(m) FROM Marcacao m WHERE m.marcacaoBalneario IS NOT NULL " +
            "AND m.estado = 'NAO_COMPARECIDO' " +
            "AND m.data BETWEEN :inicio AND :fim")
    long countBalnearioFaltas(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(m) FROM Marcacao m WHERE m.marcacaoBalneario IS NOT NULL " +
            "AND m.estado IN ('AGENDADO', 'AVISO') " +
            "AND m.data BETWEEN :inicio AND :fim")
    long countBalnearioAgendadas(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT FUNCTION('date', m.data), COUNT(m) FROM Marcacao m WHERE m.marcacaoBalneario IS NOT NULL " +
            "AND m.estado IN ('EM_PROGRESSO', 'CONCLUIDO') " +
            "AND m.data BETWEEN :inicio AND :fim " +
            "GROUP BY FUNCTION('date', m.data) " +
            "ORDER BY FUNCTION('date', m.data)")
    List<Object[]> findAttendanceByDay(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT HOUR(m.data), COUNT(m) FROM Marcacao m WHERE m.marcacaoBalneario IS NOT NULL " +
            "AND m.estado IN ('EM_PROGRESSO', 'CONCLUIDO') " +
            "AND m.data BETWEEN :inicio AND :fim " +
            "GROUP BY HOUR(m.data) " +
            "ORDER BY HOUR(m.data)")
    List<Object[]> findAttendanceByHour(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
