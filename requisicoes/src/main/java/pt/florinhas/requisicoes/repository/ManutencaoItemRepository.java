package pt.florinhas.requisicoes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.ManutencaoCategoria;
import pt.florinhas.requisicoes.domain.ManutencaoItem;

@Repository
public interface ManutencaoItemRepository extends JpaRepository<ManutencaoItem, Long> {

    List<ManutencaoItem> findByCategoria(ManutencaoCategoria categoria);

    List<ManutencaoItem> findAllByOrderByCategoriaAscEspacoAsc();
}
