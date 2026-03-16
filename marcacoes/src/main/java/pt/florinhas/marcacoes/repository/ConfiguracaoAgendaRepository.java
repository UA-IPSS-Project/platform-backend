package pt.florinhas.marcacoes.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.florinhas.marcacoes.domain.ConfiguracaoAgenda;

@Repository
public interface ConfiguracaoAgendaRepository extends JpaRepository<ConfiguracaoAgenda, Long> {

    Optional<ConfiguracaoAgenda> findByTipo(String tipo);
}