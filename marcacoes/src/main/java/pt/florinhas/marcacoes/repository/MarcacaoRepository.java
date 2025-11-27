package pt.florinhas.marcacoes.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoBalneario;
import pt.florinhas.marcacoes.domain.Utente;

@Repository
public interface MarcacaoRepository extends JpaRepository<Marcacao, Long> {
    
    // Encontrar marcações por utente
    List<Marcacao> findByUtente(Utente utente);
    
    // Encontrar marcações por funcionário
    List<Marcacao> findByFuncionario(Funcionario funcionario);
    
    // Encontrar marcações por data
    List<Marcacao> findByData(LocalDate data);
    
    // Encontrar marcações por data e estado
    List<Marcacao> findByDataAndEstado(LocalDate data, String estado);
    
    // Encontrar marcações por estado
    List<Marcacao> findByEstado(String estado);
    
    // Verificar disponibilidade - se existe marcação para o mesmo funcionário na mesma data/hora
    @Query("SELECT COUNT(m) > 0 FROM Marcacao m WHERE m.data = :data AND m.hora = :hora AND m.funcionario = :funcionario AND m.estado <> 'CANCELADO'")
    boolean existsByDataAndHoraAndFuncionarioAndEstadoNot(
        @Param("data") LocalDate data, 
        @Param("hora") LocalTime hora, 
        @Param("funcionario") Funcionario funcionario, 
        @Param("estado") String estado);
    
    // Consulta com filtros múltiplos para agenda (RF1.2.3)
    @Query("SELECT m FROM Marcacao m WHERE " +
           "(:dataInicio IS NULL OR m.data >= :dataInicio) AND " +
           "(:dataFim IS NULL OR m.data <= :dataFim) AND " +
           "(:funcionarioId IS NULL OR m.funcionario.id = :funcionarioId) AND " +
           "(:valenciaId IS NULL OR m.valencia.id = :valenciaId) AND " +
           "(:estado IS NULL OR m.estado = :estado) " +
           "ORDER BY m.data, m.hora")
    List<Marcacao> findByFilters(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim,
        @Param("funcionarioId") Long funcionarioId,
        @Param("valenciaId") Long valenciaId,
        @Param("estado") String estado);
    
    // Consultar marcações passadas com filtros (RF1.2.14)
    @Query("SELECT m FROM Marcacao m WHERE " +
           "m.data < CURRENT_DATE AND " +
           "(:dataInicio IS NULL OR m.data >= :dataInicio) AND " +
           "(:dataFim IS NULL OR m.data <= :dataFim) AND " +
           "(:utenteId IS NULL OR m.utente.id = :utenteId) AND " +
           "(:estado IS NULL OR m.estado = :estado) " +
           "ORDER BY m.data DESC, m.hora DESC")
    List<Marcacao> findMarcacoesPassadas(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim,
        @Param("utenteId") Long utenteId,
        @Param("estado") String estado);
    
    // Métodos específicos para MarcacaoBalneario
    @Query("SELECT mb FROM MarcacaoBalneario mb WHERE mb.data = :data AND (:estado IS NULL OR mb.estado = :estado)")
    List<MarcacaoBalneario> findMarcacoesBalnearioByDataAndEstado(
        @Param("data") LocalDate data, 
        @Param("estado") String estado);
    
    // Encontrar marcações de balneário por data
    @Query("SELECT mb FROM MarcacaoBalneario mb WHERE mb.data = :data")
    List<MarcacaoBalneario> findMarcacoesBalnearioByData(@Param("data") LocalDate data);
    
    // Encontrar marcações de balneário com consumo não registado
    @Query("SELECT mb FROM MarcacaoBalneario mb WHERE mb.consumoRegistado = false AND mb.data <= CURRENT_DATE")
    List<MarcacaoBalneario> findMarcacoesBalnearioComConsumoPendente();
    
    // Estatísticas - contar marcações por estado
    @Query("SELECT m.estado, COUNT(m) FROM Marcacao m GROUP BY m.estado")
    List<Object[]> countMarcacoesByEstado();
    
    // Encontrar marcações que precisam de lembretes (para RF1.2.6)
    @Query("SELECT m FROM Marcacao m WHERE m.data = CURRENT_DATE AND m.estado = 'AGENDADO'")
    List<Marcacao> findMarcacoesParaLembreteHoje();
    
    // Encontrar marcações com documentos inválidos
    List<Marcacao> findByDocumentosInvalidosTrue();
}