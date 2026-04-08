package pt.florinhas.marcacoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.florinhas.marcacoes.domain.Roupa;

/**
 * Repositório para a entidade Roupa.
 */
@Repository
public interface RoupaRepository extends JpaRepository<Roupa, Long> {

    /**
     * Verifica se existe alguma peça de roupa associada a um item do armazém.
     */
    boolean existsByItemId(Long itemId);
}
