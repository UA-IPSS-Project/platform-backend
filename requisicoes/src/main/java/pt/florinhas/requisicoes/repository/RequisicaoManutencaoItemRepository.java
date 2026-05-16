package pt.florinhas.requisicoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.RequisicaoManutencaoItem;

@Repository
public interface RequisicaoManutencaoItemRepository extends JpaRepository<RequisicaoManutencaoItem, Long> {

    boolean existsByManutencaoItemId(Long manutencaoItemId);
}