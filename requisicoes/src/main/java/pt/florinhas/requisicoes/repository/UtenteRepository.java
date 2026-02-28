package pt.florinhas.requisicoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.Utente;

@Repository
public interface UtenteRepository extends JpaRepository<Utente, Long> {

    List<Utente> findByNif(String nif);

    boolean existsByNif(String nif);

    @Query("SELECT u FROM Utente u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Utente> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    Optional<Utente> findByTelefone(String telefone);

    Optional<Utente> findByEmail(String email);

    boolean existsByTelefone(String telefone);

    boolean existsByEmail(String email);

    long countByActivo(boolean activo);
}
