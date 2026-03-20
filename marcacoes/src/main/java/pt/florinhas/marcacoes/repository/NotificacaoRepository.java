package pt.florinhas.marcacoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.florinhas.marcacoes.domain.Notificacao;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    // Listar notificações de um utilizador, mais recentes primeiro
    List<Notificacao> findByUtilizadorIdOrderByDataCriacaoDesc(Long utilizadorId);

    // Contar notificações não lidas
    // Contar notificações não lidas
    long countByUtilizadorIdAndLidaFalse(Long utilizadorId);

    Optional<Notificacao> findByIdAndUtilizadorId(Long id, Long utilizadorId);

    boolean existsByUtilizadorIdAndTituloAndMensagemAndTipo(Long utilizadorId, String titulo, String mensagem,
            pt.florinhas.marcacoes.domain.NotificacaoTipo tipo);

    // Apagar todas as notificações de um utilizador
    void deleteByUtilizadorId(Long utilizadorId);
}
