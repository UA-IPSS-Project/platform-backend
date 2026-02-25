package pt.florinhas.marcacoes.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.marcacoes.domain.Documento;
import pt.florinhas.marcacoes.domain.Marcacao;

/**
 * Repositório Spring Data JPA para a entidade Documento.
 * 
 * Responsabilidades:
 * - Operações CRUD herdadas de JpaRepository
 * - Consultas customizadas para buscar documentos por marcação
 * - Busca por nome armazenado para recuperação de ficheiros
 */
@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    /**
     * Encontra todos os documentos associados a uma marcação específica.
     * 
     * @param marcacao a marcação
     * @return lista de documentos ordenados por data de upload
     */
    @Query("SELECT d FROM Documento d WHERE d.marcacao = :marcacao ORDER BY d.uploadedEm DESC")
    List<Documento> findByMarcacao(@Param("marcacao") Marcacao marcacao);

    /**
     * Encontra todos os documentos de uma marcação pelo ID da marcação.
     * 
     * @param marcacaoId ID da marcação
     * @return lista de documentos
     */
    @Query("SELECT d FROM Documento d WHERE d.marcacao.id = :marcacaoId ORDER BY d.uploadedEm DESC")
    List<Documento> findByMarcacaoId(@Param("marcacaoId") Long marcacaoId);

    /**
     * Busca um documento pelo nome armazenado (usado para download).
     * 
     * @param nomeArmazenado nome único do ficheiro no sistema
     * @return Optional contendo o documento se encontrado
     */
    Optional<Documento> findByNomeArmazenado(String nomeArmazenado);

    /**
     * Conta quantos documentos uma marcação possui.
     * 
     * @param marcacaoId ID da marcação
     * @return número de documentos
     */
    @Query("SELECT COUNT(d) FROM Documento d WHERE d.marcacao.id = :marcacaoId")
    Long countByMarcacaoId(@Param("marcacaoId") Long marcacaoId);

    /**
     * Pesquisa documentos por metadados com filtros opcionais.
     *
     * @param marcacaoId ID da marcação
     * @param nomeOriginal parte do nome original do ficheiro
     * @param nomeArmazenado parte do nome armazenado
     * @param tipoMime tipo MIME exato
     * @param uploadedDesde limite inferior da data de upload
     * @param uploadedAte limite superior da data de upload
     * @return lista de documentos ordenada por upload mais recente
     */
    @Query("""
        SELECT d
        FROM Documento d
        WHERE (:marcacaoId IS NULL OR d.marcacao.id = :marcacaoId)
          AND (:nomeOriginal IS NULL OR LOWER(d.nomeOriginal) LIKE LOWER(CONCAT('%', :nomeOriginal, '%')))
          AND (:nomeArmazenado IS NULL OR LOWER(d.nomeArmazenado) LIKE LOWER(CONCAT('%', :nomeArmazenado, '%')))
          AND (:tipoMime IS NULL OR LOWER(d.tipo) = LOWER(:tipoMime))
          AND (:uploadedDesde IS NULL OR d.uploadedEm >= :uploadedDesde)
          AND (:uploadedAte IS NULL OR d.uploadedEm <= :uploadedAte)
        ORDER BY d.uploadedEm DESC
    """)
    List<Documento> pesquisarPorMetadados(
        @Param("marcacaoId") Long marcacaoId,
        @Param("nomeOriginal") String nomeOriginal,
        @Param("nomeArmazenado") String nomeArmazenado,
        @Param("tipoMime") String tipoMime,
        @Param("uploadedDesde") LocalDateTime uploadedDesde,
        @Param("uploadedAte") LocalDateTime uploadedAte
    );
}
