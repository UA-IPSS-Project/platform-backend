package pt.florinhas.marcacoes.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pt.florinhas.marcacoes.domain.BloqueioAgenda;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

/**
 * Repositório Spring Data JPA para a entidade {@link BloqueioAgenda}.
 *
 * Responsabilidades:
 * - Consultas CRUD standard (herdadas de JpaRepository).
 * - Consultas derivadas por convenção (ex.: findByData).
 * - Consulta custom para detetar sobreposição de intervalos num dado dia.
 * - Queries filtradas por tipo (SECRETARIA / BALNEARIO).
 */
public interface BloqueioRepository extends JpaRepository<BloqueioAgenda, Long> {

       // ── Queries SEM filtro de tipo (legacy / genéricas) ──

       List<BloqueioAgenda> findByData(LocalDate data);

       List<BloqueioAgenda> findByDataBetween(LocalDate start, LocalDate end);

       @Query("SELECT COUNT(b) > 0 FROM BloqueioAgenda b " +
                     "WHERE b.data = :data " +
                     "AND b.horaInicio < :fim " +
                     "AND b.horaFim > :inicio")
       boolean existeSobreposicao(@Param("data") LocalDate data,
                     @Param("inicio") LocalTime inicio,
                     @Param("fim") LocalTime fim);

       @Lock(LockModeType.PESSIMISTIC_WRITE)
       @Query("SELECT COUNT(b) FROM BloqueioAgenda b " +
                     "WHERE b.data = :data " +
                     "AND b.horaInicio < :fim " +
                     "AND b.horaFim > :inicio")
       long countConflictingWithLock(@Param("data") LocalDate data,
                     @Param("inicio") LocalTime inicio,
                     @Param("fim") LocalTime fim);

       // ── Queries filtradas por tipo ──

       List<BloqueioAgenda> findByDataAndTipo(LocalDate data, String tipo);

       List<BloqueioAgenda> findByDataBetweenAndTipo(LocalDate start, LocalDate end, String tipo);

       List<BloqueioAgenda> findByTipo(String tipo);

       @Lock(LockModeType.PESSIMISTIC_WRITE)
       @Query("SELECT COUNT(b) FROM BloqueioAgenda b " +
                     "WHERE b.data = :data " +
                     "AND b.horaInicio < :fim " +
                     "AND b.horaFim > :inicio " +
                     "AND b.tipo = :tipo")
       long countConflictingWithLockByTipo(@Param("data") LocalDate data,
                     @Param("inicio") LocalTime inicio,
                     @Param("fim") LocalTime fim,
                     @Param("tipo") String tipo);
}
