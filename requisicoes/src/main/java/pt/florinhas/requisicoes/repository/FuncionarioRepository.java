package pt.florinhas.requisicoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.Funcionario;
import pt.florinhas.requisicoes.domain.FuncionarioTipo;
import pt.florinhas.requisicoes.domain.Valencia;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByNif(String nif);

    boolean existsByNif(String nif);

    List<Funcionario> findByTipo(FuncionarioTipo tipo);

    List<Funcionario> findByEmail(String email);

    @Query("SELECT f FROM Funcionario f WHERE LOWER(f.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Funcionario> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    boolean existsByEmail(String email);

    @Query("SELECT f FROM Funcionario f JOIN f.valencias v WHERE v = :valencia")
    List<Funcionario> findByValencia(@Param("valencia") Valencia valencia);

    @Query("SELECT f FROM Funcionario f JOIN f.valencias v WHERE v.id = :valenciaId")
    List<Funcionario> findByValenciaId(@Param("valenciaId") Long valenciaId);

    List<Funcionario> findByActivoFalse();
}
