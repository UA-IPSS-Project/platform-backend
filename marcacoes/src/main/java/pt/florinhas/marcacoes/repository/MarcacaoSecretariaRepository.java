package pt.florinhas.marcacoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.marcacoes.domain.AtendimentoTipo;
import pt.florinhas.marcacoes.domain.Marcacao;
import pt.florinhas.marcacoes.domain.MarcacaoSecretaria;
import pt.florinhas.marcacoes.domain.Utente;

@Repository
public interface MarcacaoSecretariaRepository extends JpaRepository<MarcacaoSecretaria, Long> {
    
    // Encontrar marcação secretaria por marcação
    Optional<MarcacaoSecretaria> findByMarcacao(Marcacao marcacao);
    
    // Encontrar marcações por tipo de atendimento
    List<MarcacaoSecretaria> findByTipoAtendimento(AtendimentoTipo tipoAtendimento);
    
    // Encontrar marcações por utente
    List<MarcacaoSecretaria> findByUtente(Utente utente);
    
    // Encontrar marcações por assunto
    @Query("SELECT ms FROM MarcacaoSecretaria ms WHERE LOWER(ms.assunto) LIKE LOWER(CONCAT('%', :assunto, '%'))")
    List<MarcacaoSecretaria> findByAssuntoContainingIgnoreCase(@Param("assunto") String assunto);
}
