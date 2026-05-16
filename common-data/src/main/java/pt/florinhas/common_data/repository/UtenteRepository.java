package pt.florinhas.common_data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.common_data.domain.Utente;

/**
 * Repositório Spring Data JPA para a entidade Utente.
 *
 * Responsabilidades:
 * - Operações CRUD herdadas de JpaRepository.
 * - Consultas derivadas por convenção (findBy..., existsBy..., countBy...).
 * - Consulta JPQL customizada para pesquisa por nome (case-insensitive).
 */
@Repository
public interface UtenteRepository extends JpaRepository<Utente, Long> {

    // Encontrar utente por blind index do NIF
    List<Utente> findByNifHash(String nifHash);

    // Verificar se NIF existe (via blind index)
    boolean existsByNifHash(String nifHash);

    // Encontrar utentes por nome
    @Query("SELECT u FROM Utente u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Utente> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    // Pesquisa paginada com filtro opcional de nome ou nifHash exato
    @Query("SELECT u FROM Utente u WHERE " +
           "(:nifHash IS NOT NULL AND u.nifHash = :nifHash) OR " +
           "(:nifHash IS NULL AND (:nome IS NULL OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%'))))")
    Page<Utente> findByNomeFilter(@Param("nome") String nome, @Param("nifHash") String nifHash, Pageable pageable);

    // Encontrar utente por telefone
    Optional<Utente> findByTelefone(String telefone);

    // Encontrar utente por email
    Optional<Utente> findByEmail(String email);

    // Verificar se telefone existe
    boolean existsByTelefone(String telefone);

    // Verificar se email existe
    boolean existsByEmail(String email);

    // Contar total de utentes
    @Override
    long count();

    // Contar utentes ativos
    long countByActivo(boolean activo);

}