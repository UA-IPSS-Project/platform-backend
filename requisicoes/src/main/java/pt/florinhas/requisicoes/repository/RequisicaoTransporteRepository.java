package pt.florinhas.requisicoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.RequisicaoTransporte;

@Repository
public interface RequisicaoTransporteRepository extends JpaRepository<RequisicaoTransporte, Long> {

	boolean existsByTransporteId(Long transporteId);
}
