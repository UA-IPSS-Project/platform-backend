package pt.florinhas.marcacoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.marcacoes.domain.Valencia;

@Repository
public interface ValenciaRepository extends JpaRepository<Valencia, Long> {
    
    // Encontrar valência por nome
    Optional<Valencia> findByNome(String nome);
    
    // Verificar se valência com nome existe
    boolean existsByNome(String nome);
    
    // Encontrar valências por nome (case insensitive search)
    @Query("SELECT v FROM Valencia v WHERE LOWER(v.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Valencia> findByNomeContainingIgnoreCase(@Param("nome") String nome);
    
    // Ordenar valências por nome
    List<Valencia> findAllByOrderByNomeAsc();
    
    // Contar valências
    @Override
    long count();
}