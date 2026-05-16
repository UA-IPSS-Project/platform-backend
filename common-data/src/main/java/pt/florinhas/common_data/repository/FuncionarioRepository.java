package pt.florinhas.common_data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.common_data.domain.Funcionario;
import pt.florinhas.common_data.domain.FuncionarioTipo;
import pt.florinhas.common_data.domain.Valencia;

/**
 * Repositório Spring Data JPA para a entidade Funcionario.
 *
 * Responsabilidades:
 * - Operações CRUD herdadas de JpaRepository.
 * - Consultas derivadas por convenção (findBy.../existsBy...).
 * - Consultas JPQL customizadas para pesquisa por nome e por valência.
 */
@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    // Encontrar funcionário por blind index do NIF
    Optional<Funcionario> findByNifHash(String nifHash);

    // Verificar se NIF existe (via blind index)
    boolean existsByNifHash(String nifHash);

    // Encontrar funcionários por tipo (SECRETARIA, BALNEARIO)
    List<Funcionario> findByTipo(FuncionarioTipo tipo);

    // Encontrar funcionários por email
    List<Funcionario> findByEmail(String email);

    // Buscar funcionários por nome (case insensitive)
    @Query("SELECT f FROM Funcionario f WHERE LOWER(f.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Funcionario> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    // Verificar se existe funcionário com email
    boolean existsByEmail(String email);

    // Encontrar funcionários por valência
    @Query("SELECT f FROM Funcionario f JOIN f.valencias v WHERE v = :valencia")
    List<Funcionario> findByValencia(@Param("valencia") Valencia valencia);

    // Encontrar funcionários por ID de valência
    @Query("SELECT f FROM Funcionario f JOIN f.valencias v WHERE v.id = :valenciaId")
    List<Funcionario> findByValenciaId(@Param("valenciaId") Long valenciaId);

    // Encontrar funcionários pendentes de aprovação
    List<Funcionario> findByActivoFalse();

    // Pesquisa paginada com filtro opcional de nome e tipo
    // Pesquisa paginada com filtro opcional de nome, tipo e nifHash exato
    @Query("SELECT f FROM Funcionario f WHERE " +
           "(:nifHash IS NOT NULL AND f.nifHash = :nifHash) OR " +
           "(:nifHash IS NULL AND (:nome IS NULL OR LOWER(f.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND (:tipo IS NULL OR f.tipo = :tipo))")
    Page<Funcionario> findByNomeAndTipoFilter(
            @Param("nome") String nome,
            @Param("tipo") FuncionarioTipo tipo,
            @Param("nifHash") String nifHash,
            Pageable pageable);
}