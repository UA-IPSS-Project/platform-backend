package pt.florinhas.marcacoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.florinhas.marcacoes.domain.Assunto;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssuntoRepository extends JpaRepository<Assunto, Long> {
    List<Assunto> findByAtivoTrue();
    Optional<Assunto> findByNome(String nome);
}
