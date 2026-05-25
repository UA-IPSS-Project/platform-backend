package pt.florinhas.marcacoes.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import pt.florinhas.marcacoes.domain.ConfiguracaoAgenda;

@Repository
public interface ConfiguracaoAgendaRepository extends JpaRepository<ConfiguracaoAgenda, Long> {

    Optional<ConfiguracaoAgenda> findByTipo(String tipo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ConfiguracaoAgenda c WHERE c.tipo = :tipo")
    Optional<ConfiguracaoAgenda> findByTipoWithWriteLock(@Param("tipo") String tipo);
}