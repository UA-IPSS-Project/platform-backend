package pt.florinhas.marcacoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.marcacoes.domain.Utilizador;
/**
 * Repositório Spring Data JPA para a entidade Utilizador.
 *
 * Responsabilidades:
 *  - Operações CRUD herdadas de JpaRepository.
 *  - Consultas derivadas por convenção (findBy..., existsBy...).
 *  - Consulta JPQL customizada para pesquisa por nome (case-insensitive).
 */
@Repository
public interface UtilizadorRepository extends JpaRepository<Utilizador, Long> {
    
    // Encontrar utilizador por email
    Optional<Utilizador> findByEmail(String email);
    
    // Encontrar utilizador por NIF
    Optional<Utilizador> findByNif(String nif);
    
    // Encontrar utilizadores por nome
    @Query("SELECT u FROM Utilizador u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Utilizador> findByNomeContainingIgnoreCase(@Param("nome") String nome);
    
    // Verificar se email existe
    boolean existsByEmail(String email);
    
    // Verificar se NIF existe
    boolean existsByNif(String nif);
    
    // Encontrar utilizador por telefone
    Optional<Utilizador> findByTelefone(String telefone);
    
    // Verificar se telefone existe
    boolean existsByTelefone(String telefone);
}
