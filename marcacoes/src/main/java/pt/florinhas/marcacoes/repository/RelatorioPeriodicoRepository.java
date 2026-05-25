package pt.florinhas.marcacoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.florinhas.marcacoes.domain.RelatorioPeriodico;

import java.util.List;

public interface RelatorioPeriodicoRepository extends JpaRepository<RelatorioPeriodico, Long> {
    List<RelatorioPeriodico> findByActivoTrue();
}
