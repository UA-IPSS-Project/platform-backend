package pt.florinhas.requisicoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.Notificacao;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByUtilizadorIdOrderByDataCriacaoDesc(Long utilizadorId);

    long countByUtilizadorIdAndLidaFalse(Long utilizadorId);

    Optional<Notificacao> findByIdAndUtilizadorId(Long id, Long utilizadorId);

    void deleteByUtilizadorId(Long utilizadorId);
}
