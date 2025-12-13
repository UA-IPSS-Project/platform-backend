package pt.florinhas.marcacoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.marcacoes.domain.Utente;
/**
 * Repositório Spring Data JPA para a entidade Utente.
 *
 * Responsabilidades:
 *  - Operações CRUD herdadas de JpaRepository.
 *  - Consultas derivadas por convenção (findBy..., existsBy..., countBy...).
 *  - Consulta JPQL customizada para pesquisa por nome (case-insensitive).
 */
@Repository
public interface UtenteRepository extends JpaRepository<Utente, Long> {
    
    // Encontrar utente por NIF
    Optional<Utente> findByNif(String nif);
    
    // Verificar se NIF existe
    boolean existsByNif(String nif);
    
    // Encontrar utentes por nome
    @Query("SELECT u FROM Utente u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Utente> findByNomeContainingIgnoreCase(@Param("nome") String nome);
    
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