package pt.florinhas.requisicoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.Material;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

	boolean existsByNomeIgnoreCase(String nome);

	java.util.List<Material> findAllByOrderByCategoriaAscNomeAscAtributoAscValorAtributoAsc();
}
