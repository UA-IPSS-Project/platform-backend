package pt.florinhas.marcacoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.florinhas.marcacoes.domain.ItemArmazem;

/**
 * Repositório para a entidade ItemArmazem.
 */
@Repository
public interface ItemArmazemRepository extends JpaRepository<ItemArmazem, Long> {

    List<ItemArmazem> findByCategoria(String categoria);

    Optional<ItemArmazem> findByCategoriaAndNome(String categoria, String nome);

    List<ItemArmazem> findAllByOrderByCategoriaAscNomeAsc();
}