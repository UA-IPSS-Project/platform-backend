package pt.florinhas.marcacoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.marcacoes.domain.Utente;

@Repository
public interface UtenteRepository extends JpaRepository<Utente, Long> {
    
    // Encontrar utente por NIF
    Optional<Utente> findByNif(String nif);
    
    // Verificar se NIF existe
    boolean existsByNif(String nif);
    
    // Encontrar utentes por nome
    @Query("SELECT u FROM Utente u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Utente> findByNomeContainingIgnoreCase(@Param("nome") String nome);
    
    // Encontrar utentes com contas criadas automaticamente
    List<Utente> findByContaCriadaAutomaticamenteTrue();
    
    // Encontrar utentes que ainda não definiram password
    List<Utente> findByPasswordDefinidaFalse();
    
    // Encontrar utente por telefone
    Optional<Utente> findByTelefone(String telefone);
    
    // Encontrar utente por email
    Optional<Utente> findByEmail(String email);
    
    // Verificar se telefone existe
    boolean existsByTelefone(String telefone);
    
    // Verificar se email existe
    boolean existsByEmail(String email);
    
    // Buscar utentes com contas automáticas e sem password definida (para RF1.2.15)
    @Query("SELECT u FROM Utente u WHERE u.contaCriadaAutomaticamente = true AND u.passwordDefinida = false")
    List<Utente> findUtentesComContaAutomaticaSemPassword();
    
    // Contar total de utentes
    long count();
    
    // Encontrar utentes com marcações ativas
    @Query("SELECT DISTINCT u FROM Utente u JOIN u.marcacoes m WHERE m.estado = 'AGENDADO'")
    List<Utente> findUtentesComMarcacoesAtivas();
}