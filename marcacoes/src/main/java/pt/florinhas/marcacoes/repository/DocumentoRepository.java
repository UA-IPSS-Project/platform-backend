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
import pt.florinhas.common_data.domain.Utente;

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
     * Encontra todos os documentos de um utente específico.
     * 
     * @param utente o utente alvo
     * @return lista de documentos
     */
    @Query("SELECT d FROM Documento d WHERE d.marcacao.marcacaoSecretaria.utente = :utente ORDER BY d.uploadedEm DESC")
    List<Documento> findByUtente(@Param("utente") Utente utente);

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
     * Obtém o maior valor de sequência entre os documentos de uma marcação.
     * 
     * @param marcacaoId ID da marcação
     * @return maior sequência encontrada ou Optional vazio
     */
    @Query("SELECT MAX(d.sequencia) FROM Documento d WHERE d.marcacao.id = :marcacaoId")
    Optional<Integer> findMaxSequenciaByMarcacaoId(@Param("marcacaoId") Long marcacaoId);

    /**
     * Conta quantos documentos uma marcação possui.
     * 
     * @param marcacaoId ID da marcação
     * @return número de documentos
     */
    @Query("SELECT COUNT(d) FROM Documento d WHERE d.marcacao.id = :marcacaoId")
    Long countByMarcacaoId(@Param("marcacaoId") Long marcacaoId);

    List<Documento> findAllByOrderByUploadedEmDesc();

    List<Documento> findByMarcacaoIdOrderByUploadedEmDesc(Long marcacaoId);

    List<Documento> findByUploadedEmGreaterThanEqualOrderByUploadedEmDesc(LocalDateTime uploadedDesde);

    List<Documento> findByUploadedEmLessThanEqualOrderByUploadedEmDesc(LocalDateTime uploadedAte);

    List<Documento> findByUploadedEmBetweenOrderByUploadedEmDesc(LocalDateTime uploadedDesde, LocalDateTime uploadedAte);

    List<Documento> findByMarcacaoIdAndUploadedEmGreaterThanEqualOrderByUploadedEmDesc(Long marcacaoId, LocalDateTime uploadedDesde);

    List<Documento> findByMarcacaoIdAndUploadedEmLessThanEqualOrderByUploadedEmDesc(Long marcacaoId, LocalDateTime uploadedAte);

    List<Documento> findByMarcacaoIdAndUploadedEmBetweenOrderByUploadedEmDesc(Long marcacaoId, LocalDateTime uploadedDesde, LocalDateTime uploadedAte);

    @Query("SELECT d FROM Documento d WHERE d.marcacao.data >= :desde ORDER BY d.uploadedEm DESC")
    List<Documento> findByMarcacaoDataGreaterThanEqual(@Param("desde") LocalDateTime desde);

    @Query("SELECT d FROM Documento d WHERE d.marcacao.data <= :ate ORDER BY d.uploadedEm DESC")
    List<Documento> findByMarcacaoDataLessThanEqual(@Param("ate") LocalDateTime ate);

    @Query("SELECT d FROM Documento d WHERE d.marcacao.data >= :desde AND d.marcacao.data <= :ate ORDER BY d.uploadedEm DESC")
    List<Documento> findByMarcacaoDataBetween(@Param("desde") LocalDateTime desde, @Param("ate") LocalDateTime ate);

    @Query("SELECT d FROM Documento d WHERE d.marcacao.id = :marcacaoId AND d.marcacao.data >= :desde AND d.marcacao.data <= :ate ORDER BY d.uploadedEm DESC")
    List<Documento> findByMarcacaoIdAndMarcacaoDataBetween(@Param("marcacaoId") Long marcacaoId, @Param("desde") LocalDateTime desde, @Param("ate") LocalDateTime ate);

    /**
     * Encontra documentos expirados (data de expiração anterior à data fornecida).
     * 
     * @param dataLimite data limite para considerar expirado
     * @return lista de documentos expirados ordenados por data de expiração
     */
    List<Documento> findByDataExpiracaoBeforeOrderByDataExpiracaoAsc(LocalDateTime dataLimite);
}
