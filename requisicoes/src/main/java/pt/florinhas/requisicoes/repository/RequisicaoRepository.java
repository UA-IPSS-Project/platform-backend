package pt.florinhas.requisicoes.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = {"criadoPor", "geridoPor"})
    @Override
    List<Requisicao> findAll();

    @EntityGraph(attributePaths = {"criadoPor", "geridoPor"})
    List<Requisicao> findByEstado(RequisicaoEstado estado);

    List<Requisicao> findByTipo(RequisicaoTipo tipo);

        List<Requisicao> findByCriadoPorNomeContainingIgnoreCase(String nome);

    @Query("SELECT r FROM Requisicao r " +
            "JOIN FETCH r.criadoPor c " +
            "LEFT JOIN FETCH r.geridoPor " +
            "WHERE " +
            "(CAST(:estado AS string) IS NULL OR r.estado = :estado) AND " +
            "(CAST(:tipo AS string) IS NULL OR r.tipo = :tipo) AND " +
            "(CAST(:prioridade AS string) IS NULL OR r.prioridade = :prioridade) AND " +
            "(:criadoPorNome IS NULL OR LOWER(c.nome) LIKE :criadoPorNome) AND " +
            "(CAST(:dataInicio AS timestamp) IS NULL OR r.criadoEm >= :dataInicio) AND " +
            "(CAST(:dataFim AS timestamp) IS NULL OR r.criadoEm <= :dataFim) " +
            "ORDER BY r.criadoEm DESC")
    List<Requisicao> findWithFilters(
            @Param("estado") RequisicaoEstado estado,
            @Param("tipo") RequisicaoTipo tipo,
            @Param("prioridade") RequisicaoPrioridade prioridade,
            @Param("criadoPorNome") String criadoPorNome,
            @Param("dataInicio") java.time.LocalDateTime dataInicio,
            @Param("dataFim") java.time.LocalDateTime dataFim);

    // JOIN sem FETCH para o filtro de nome — @EntityGraph carrega as relações em passo separado,
    // o que permite ao Spring Data aplicar LIMIT/OFFSET em SQL (evita paginação em memória).
    @EntityGraph(attributePaths = {"criadoPor", "geridoPor"})
    @Query(value = "SELECT r FROM Requisicao r " +
            "JOIN r.criadoPor c " +
            "WHERE " +
            "(CAST(:estado AS string) IS NULL OR r.estado = :estado) AND " +
            "(CAST(:tipo AS string) IS NULL OR r.tipo = :tipo) AND " +
            "(CAST(:prioridade AS string) IS NULL OR r.prioridade = :prioridade) AND " +
            "(:criadoPorNome IS NULL OR LOWER(c.nome) LIKE :criadoPorNome) AND " +
            "(CAST(:dataInicio AS timestamp) IS NULL OR r.criadoEm >= :dataInicio) AND " +
            "(CAST(:dataFim AS timestamp) IS NULL OR r.criadoEm <= :dataFim)",
            countQuery = "SELECT COUNT(r) FROM Requisicao r LEFT JOIN r.criadoPor c " +
            "WHERE (CAST(:estado AS string) IS NULL OR r.estado = :estado) AND " +
            "(CAST(:tipo AS string) IS NULL OR r.tipo = :tipo) AND " +
            "(CAST(:prioridade AS string) IS NULL OR r.prioridade = :prioridade) AND " +
            "(:criadoPorNome IS NULL OR LOWER(c.nome) LIKE :criadoPorNome) AND " +
            "(CAST(:dataInicio AS timestamp) IS NULL OR r.criadoEm >= :dataInicio) AND " +
            "(CAST(:dataFim AS timestamp) IS NULL OR r.criadoEm <= :dataFim)")
    Page<Requisicao> findWithFiltersPaginated(
            @Param("estado") RequisicaoEstado estado,
            @Param("tipo") RequisicaoTipo tipo,
            @Param("prioridade") RequisicaoPrioridade prioridade,
            @Param("criadoPorNome") String criadoPorNome,
            @Param("dataInicio") java.time.LocalDateTime dataInicio,
            @Param("dataFim") java.time.LocalDateTime dataFim,
            Pageable pageable);
}
