package pt.florinhas.marcacoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.marcacoes.domain.Funcionario;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    
    // Encontrar funcionário por NIF
    Optional<Funcionario> findByNif(String nif);
    
    // Verificar se NIF existe
    boolean existsByNif(String nif);
    
    // Encontrar funcionários por tipo (SECRETARIA, BALNEARIO, TECNICO)
    List<Funcionario> findByTipo(String tipo);
    
    // Encontrar funcionários por email
    Optional<Funcionario> findByEmail(String email);
    
    // Encontrar funcionários ativos (se tivermos campo de ativo)
    // List<Funcionario> findByAtivoTrue();
    
    // Buscar funcionários por nome (case insensitive)
    @Query("SELECT f FROM Funcionario f WHERE LOWER(f.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Funcionario> findByNomeContainingIgnoreCase(@Param("nome") String nome);
    
    // Verificar se existe funcionário com email
    boolean existsByEmail(String email);
}