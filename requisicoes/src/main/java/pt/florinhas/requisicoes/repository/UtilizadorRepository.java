package pt.florinhas.requisicoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.Utilizador;

@Repository
public interface UtilizadorRepository extends JpaRepository<Utilizador, Long> {

    List<Utilizador> findByEmail(String email);

    List<Utilizador> findByNif(String nif);

    @Query("SELECT u FROM Utilizador u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Utilizador> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    boolean existsByEmail(String email);

    boolean existsByNif(String nif);

    Optional<Utilizador> findByTelefone(String telefone);

    boolean existsByTelefone(String telefone);
}
