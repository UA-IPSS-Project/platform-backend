package pt.florinhas.requisicoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.TipoManutencaoCatalogo;

@Repository
public interface TipoManutencaoCatalogoRepository extends JpaRepository<TipoManutencaoCatalogo, Long> {

    List<TipoManutencaoCatalogo> findAllByOrderByNomeAsc();

    Optional<TipoManutencaoCatalogo> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);
}