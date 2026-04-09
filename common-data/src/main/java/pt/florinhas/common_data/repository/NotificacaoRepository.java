package pt.florinhas.common_data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.florinhas.common_data.domain.Notificacao;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    // Listar notificações de um utilizador, mais recentes primeiro
    List<Notificacao> findByUtilizadorIdOrderByDataCriacaoDesc(Long utilizadorId);

    // Contar notificações não lidas
    // Contar notificações não lidas
    long countByUtilizadorIdAndLidaFalse(Long utilizadorId);

    Optional<Notificacao> findByIdAndUtilizadorId(Long id, Long utilizadorId);

    boolean existsByUtilizadorIdAndTituloAndMensagemAndTipo(Long utilizadorId, String titulo, String mensagem,
            pt.florinhas.common_data.domain.NotificacaoTipo tipo);

    // Apagar todas as notificações de um utilizador
    void deleteByUtilizadorId(Long utilizadorId);
}
