package pt.florinhas.marcacoes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.florinhas.marcacoes.domain.Notificacao;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    // Listar notificações de um utilizador, mais recentes primeiro
    List<Notificacao> findByUtilizadorIdOrderByDataCriacaoDesc(Long utilizadorId);

    // Contar notificações não lidas
    long countByUtilizadorIdAndLidaFalse(Long utilizadorId);
}
