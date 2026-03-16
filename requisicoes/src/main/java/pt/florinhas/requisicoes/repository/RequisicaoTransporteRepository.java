package pt.florinhas.requisicoes.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.RequisicaoEstado;
import pt.florinhas.requisicoes.domain.RequisicaoTransporte;

@Repository
public interface RequisicaoTransporteRepository extends JpaRepository<RequisicaoTransporte, Long> {

    @Query("""
	    SELECT DISTINCT r
	    FROM RequisicaoTransporte r
	    JOIN r.transportes item
	    WHERE r.estado = :estado
	      AND (:requisicaoIgnoradaId IS NULL OR r.id <> :requisicaoIgnoradaId)
	      AND item.transporte.id IN :transporteIds
	      AND r.dataHoraSaida < :dataHoraRegresso
	      AND r.dataHoraRegresso > :dataHoraSaida
	    """)
    List<RequisicaoTransporte> findConflitosTransporte(
	    @Param("estado") RequisicaoEstado estado,
	    @Param("transporteIds") List<Long> transporteIds,
	    @Param("dataHoraSaida") LocalDateTime dataHoraSaida,
	    @Param("dataHoraRegresso") LocalDateTime dataHoraRegresso,
	    @Param("requisicaoIgnoradaId") Long requisicaoIgnoradaId);
}
