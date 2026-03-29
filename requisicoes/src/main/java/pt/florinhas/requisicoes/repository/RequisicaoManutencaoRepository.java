package pt.florinhas.requisicoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.RequisicaoManutencao;

@Repository
public interface RequisicaoManutencaoRepository extends JpaRepository<RequisicaoManutencao, Long> {

	boolean existsByAssuntoIgnoreCase(String assunto);
	boolean existsByDescricao(String descricao);
}
