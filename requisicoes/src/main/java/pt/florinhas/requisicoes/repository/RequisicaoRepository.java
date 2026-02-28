package pt.florinhas.requisicoes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.Requisicao;
import pt.florinhas.requisicoes.domain.RequisicaoEstado;
import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;
import pt.florinhas.requisicoes.domain.RequisicaoTipo;

@Repository
public interface RequisicaoRepository extends JpaRepository<Requisicao, Long> {

    List<Requisicao> findByEstado(RequisicaoEstado estado);

    List<Requisicao> findByTipo(RequisicaoTipo tipo);

    List<Requisicao> findByCriadoPorId(Long funcionarioId);

    @Query("SELECT r FROM Requisicao r " +
            "WHERE " +
            "(:estado IS NULL OR r.estado = :estado) AND " +
            "(:tipo IS NULL OR r.tipo = :tipo) AND " +
            "(:prioridade IS NULL OR r.prioridade = :prioridade) AND " +
            "(:criadoPorId IS NULL OR r.criadoPor.id = :criadoPorId) AND " +
            "(:geridoPorId IS NULL OR r.geridoPor.id = :geridoPorId) " +
            "ORDER BY r.criadoEm DESC")
    List<Requisicao> findWithFilters(
            @Param("estado") RequisicaoEstado estado,
            @Param("tipo") RequisicaoTipo tipo,
            @Param("prioridade") RequisicaoPrioridade prioridade,
            @Param("criadoPorId") Long criadoPorId,
            @Param("geridoPorId") Long geridoPorId);
}
