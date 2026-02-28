package pt.florinhas.requisicoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.RequisicaoMaterial;

@Repository
public interface RequisicaoMaterialRepository extends JpaRepository<RequisicaoMaterial, Long> {
}
