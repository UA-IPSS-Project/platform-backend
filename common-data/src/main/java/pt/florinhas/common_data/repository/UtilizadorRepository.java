package pt.florinhas.common_data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.common_data.domain.Utilizador;

/**
 * Repositório Spring Data JPA para a entidade Utilizador.
 *
 * Responsabilidades:
 * - Operações CRUD herdadas de JpaRepository.
 * - Consultas derivadas por convenção (findBy..., existsBy...).
 * - Consulta JPQL customizada para pesquisa por nome (case-insensitive).
 */
@Repository
public interface UtilizadorRepository extends JpaRepository<Utilizador, Long> {

    // Encontrar utilizador por email (List para tolerar duplicados)
    List<Utilizador> findByEmail(String email);

    // Encontrar utilizador por blind index do NIF
    List<Utilizador> findByNifHash(String nifHash);

    // Encontrar utilizadores por nome
    @Query("SELECT u FROM Utilizador u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Utilizador> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    // Verificar se email existe
    boolean existsByEmail(String email);

    // Verificar se NIF existe (via blind index)
    boolean existsByNifHash(String nifHash);

    // Encontrar utilizador por telefone
    Optional<Utilizador> findByTelefone(String telefone);

    // Verificar se telefone existe
    boolean existsByTelefone(String telefone);

    // Buscar utilizadores com termos desatualizados (query feita na BD, não em memória)
    @Query("""
        select u from Utilizador u
        where u.email is not null
        and u.email not like '%@anonimizado.local'
        and (u.termsVersion is null or u.termsVersion < :newVersion)
        """)
    List<Utilizador> findOutdatedTermsUsers(@Param("newVersion") int newVersion);
}