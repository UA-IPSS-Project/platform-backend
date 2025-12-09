package pt.florinhas.marcacoes.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.marcacoes.domain.EventoEstado;
import pt.florinhas.marcacoes.domain.Utilizador;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.Utente;

@Repository
public interface MarcacaoRepository extends JpaRepository<Marcacao, Long> {
    
    // Encontrar marcações por utente (através de MarcacaoSecretaria)
    @Query("SELECT m FROM Marcacao m WHERE m.marcacaoSecretaria.utente = :utente")
    List<Marcacao> findByUtente(@Param("utente") Utente utente);
    
    // Encontrar marcações criadas por utilizador
    @Query("SELECT m FROM Marcacao m WHERE m.criadoPor = :criadoPor")
    List<Marcacao> findByCriadoPor(@Param("criadoPor") Utilizador criadoPor);
    
    // Encontrar marcações por estado
    List<Marcacao> findByEstado(EventoEstado estado);
    
    // Encontrar marcações entre datas
    @Query("SELECT m FROM Marcacao m WHERE m.data BETWEEN :dataInicio AND :dataFim ORDER BY m.data")
    List<Marcacao> findMarcacoesBetweenDates(
        @Param("dataInicio") LocalDateTime dataInicio, 
        @Param("dataFim") LocalDateTime dataFim);
    
    // Verificar se existe marcação na mesma data (não cancelada)
    @Query("SELECT COUNT(m) > 0 FROM Marcacao m WHERE CAST(m.data AS date) = CAST(:data AS date) AND m.estado <> :estado")
    boolean existsByDataAndEstadoNot(
        @Param("data") LocalDateTime data, 
        @Param("estado") EventoEstado estado);
    
    // Consulta com filtros múltiplos para agenda
    @Query("SELECT m FROM Marcacao m WHERE " +
           "(:dataInicio IS NULL OR m.data >= :dataInicio) AND " +
           "(:dataFim IS NULL OR m.data <= :dataFim) AND " +
           "(:criadoPorId IS NULL OR m.criadoPor.id = :criadoPorId) AND " +
           "(:estado IS NULL OR m.estado = :estado) " +
           "ORDER BY m.data")
    List<Marcacao> findWithFilters(
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim,
        @Param("criadoPorId") Long criadoPorId,
        @Param("estado") EventoEstado estado);
    
    // Consultar marcações passadas com filtros
    @Query("SELECT m FROM Marcacao m WHERE " +
           "m.data < :now AND " +
           "(:dataInicio IS NULL OR m.data >= :dataInicio) AND " +
           "(:dataFim IS NULL OR m.data <= :dataFim) AND " +
           "(:utenteId IS NULL OR m.marcacaoSecretaria.utente.id = :utenteId) AND " +
           "(:estado IS NULL OR m.estado = :estado) " +
           "ORDER BY m.data DESC")
    List<Marcacao> findMarcacoesPassadas(
        @Param("now") LocalDateTime now,
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim,
        @Param("utenteId") Long utenteId,
        @Param("estado") EventoEstado estado);
        
    // Estatísticas - contar marcações por estado
    @Query("SELECT m.estado, COUNT(m) FROM Marcacao m GROUP BY m.estado")
    List<Object[]> countMarcacoesByEstado();
    
}