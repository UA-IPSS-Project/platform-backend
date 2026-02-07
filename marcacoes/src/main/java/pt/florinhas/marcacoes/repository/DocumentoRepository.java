package pt.florinhas.marcacoes.repository;

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
}
