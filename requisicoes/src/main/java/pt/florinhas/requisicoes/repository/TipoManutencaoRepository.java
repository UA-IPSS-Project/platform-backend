package pt.florinhas.requisicoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.TipoManutencao;

@Repository
public interface TipoManutencaoRepository extends JpaRepository<TipoManutencao, Long> {

    List<TipoManutencao> findAllByOrderByNomeAsc();

    Optional<TipoManutencao> findByNomeIgnoreCase(String nome);
}
