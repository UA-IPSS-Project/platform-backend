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

        List<Requisicao> findByCriadoPorNomeContainingIgnoreCase(String nome);

    @Query("SELECT r FROM Requisicao r " +
            "LEFT JOIN r.criadoPor c " +
            "LEFT JOIN r.geridoPor g " +
            "WHERE " +
            "(:estado IS NULL OR r.estado = :estado) AND " +
            "(:tipo IS NULL OR r.tipo = :tipo) AND " +
            "(:prioridade IS NULL OR r.prioridade = :prioridade) AND " +
            "(:criadoPorNome IS NULL OR LOWER(c.nome) LIKE :criadoPorNome) AND " +
            "(:geridoPorNome IS NULL OR (g IS NOT NULL AND LOWER(g.nome) LIKE :geridoPorNome)) " +
            "ORDER BY r.criadoEm DESC")
    List<Requisicao> findWithFilters(
            @Param("estado") RequisicaoEstado estado,
            @Param("tipo") RequisicaoTipo tipo,
            @Param("prioridade") RequisicaoPrioridade prioridade,
            @Param("criadoPorNome") String criadoPorNome,
            @Param("geridoPorNome") String geridoPorNome);
}
