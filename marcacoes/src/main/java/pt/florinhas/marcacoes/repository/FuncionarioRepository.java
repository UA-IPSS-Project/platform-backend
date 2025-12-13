package pt.florinhas.marcacoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.FuncionarioTipo;
import pt.florinhas.marcacoes.domain.Valencia;
/**
 * Repositório Spring Data JPA para a entidade Funcionario.
 *
 * Responsabilidades:
 *  - Operações CRUD herdadas de JpaRepository.
 *  - Consultas derivadas por convenção (findBy.../existsBy...).
 *  - Consultas JPQL customizadas para pesquisa por nome e por valência.
 */
@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    
    // Encontrar funcionário por NIF
    Optional<Funcionario> findByNif(String nif);
    
    // Verificar se NIF existe
    boolean existsByNif(String nif);
    
    // Encontrar funcionários por tipo (SECRETARIA, BALNEARIO)
    List<Funcionario> findByTipo(FuncionarioTipo tipo);
    
    // Encontrar funcionários por email
    Optional<Funcionario> findByEmail(String email);
    
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
}