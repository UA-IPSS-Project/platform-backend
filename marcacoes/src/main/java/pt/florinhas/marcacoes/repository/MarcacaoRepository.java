package pt.florinhas.marcacoes.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.Utente;
import pt.florinhas.marcacoes.domain.Utilizador;

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

        // Encontrar marcações por utente (através de MarcacaoSecretaria)
        @Query("SELECT m FROM Marcacao m WHERE m.marcacaoSecretaria.utente = :utente")
        List<Marcacao> findByUtente(@Param("utente") Utente utente);

        // Encontrar marcações criadas por utilizador
        @Query("SELECT m FROM Marcacao m WHERE m.criadoPor = :criadoPor")
        List<Marcacao> findByCriadoPor(@Param("criadoPor") Utilizador criadoPor);

        // Encontrar marcações por estado
        List<Marcacao> findByEstado(EventoEstado estado);

        // Encontrar marcações entre datas com JOIN FETCH para evitar N+1
        @Query("SELECT DISTINCT m FROM Marcacao m " +
                        "LEFT JOIN FETCH m.marcacaoSecretaria ms " +
                        "LEFT JOIN FETCH ms.utente u " +
                        "LEFT JOIN FETCH m.criadoPor cp " +
                        "WHERE m.data BETWEEN :dataInicio AND :dataFim AND m.estado <> 'CANCELADO' " +
                        "ORDER BY m.data")
        List<Marcacao> findMarcacoesBetweenDates(
                        @Param("dataInicio") LocalDateTime dataInicio,
                        @Param("dataFim") LocalDateTime dataFim);

        // Verificar se existe marcação no mesmo horário exato (não cancelada)
        @Query("SELECT COUNT(m) > 0 FROM Marcacao m WHERE m.data = :data AND m.estado <> :estado")
        boolean existsByDataAndEstadoNot(
                        @Param("data") LocalDateTime data,
                        @Param("estado") EventoEstado estado);

        // Consulta com Lock Pessimista para evitar Race Conditions
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT m FROM Marcacao m WHERE m.data = :data AND m.estado <> 'CANCELADO'")
        List<Marcacao> findConflictingWithLock(@Param("data") LocalDateTime data);

        // Consulta com filtros múltiplos para agenda (Optimized)
        @Query("SELECT DISTINCT m FROM Marcacao m " +
                        "LEFT JOIN FETCH m.marcacaoSecretaria ms " +
                        "LEFT JOIN FETCH ms.utente u " +
                        "LEFT JOIN FETCH m.criadoPor cp " +
                        "WHERE " +
                        "(:dataInicio IS NULL OR m.data >= :dataInicio) AND " +
                        "(:dataFim IS NULL OR m.data <= :dataFim) AND " +
                        "(:criadoPorId IS NULL OR m.criadoPor.id = :criadoPorId) AND " +
                        "(:estado IS NULL OR m.estado = :estado) " +
                        "ORDER BY m.data")
        List<Marcacao> findWithFilters(
                        @Param("dataInicio") LocalDateTime dataInicio,
                        @Param("dataFim") LocalDateTime dataFim,
                        @Param("criadoPorId") Long criadoPorId,
                        @Param("estado") EventoEstado estado);

        // Consultar marcações passadas com filtros (Optimized)
        @Query("SELECT DISTINCT m FROM Marcacao m " +
                        "LEFT JOIN FETCH m.marcacaoSecretaria ms " +
                        "LEFT JOIN FETCH ms.utente u " +
                        "LEFT JOIN FETCH m.criadoPor cp " +
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
        void deleteByEstadoAndCriadoEmBefore(EventoEstado emPreenchimento, LocalDateTime limite);

        /**
         * Remove marcações "EM_PREENCHIMENTO" que estejam expiradas OU com timestamp
         * nulo
         * (para corrigir bugs antigos onde o timestamp não era gravado).
         */
        @Modifying
        @Query("DELETE FROM Marcacao m WHERE m.estado = :estado AND (m.criadoEm < :limite OR m.criadoEm IS NULL)")
        void deleteExpiredOrorphan(@Param("estado") EventoEstado estado, @Param("limite") LocalDateTime limite);

        // Consulta paginada com fetch para evitar N+1
        @Query(value = "SELECT DISTINCT m FROM Marcacao m " +
                        "LEFT JOIN FETCH m.marcacaoSecretaria ms " +
                        "LEFT JOIN FETCH ms.utente u " +
                        "LEFT JOIN FETCH m.criadoPor cp", countQuery = "SELECT COUNT(m) FROM Marcacao m")
        Page<Marcacao> findAllWithRelations(Pageable pageable);
}
