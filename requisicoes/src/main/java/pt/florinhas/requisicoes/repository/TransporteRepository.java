package pt.florinhas.requisicoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.domain.TransporteCategoria;

@Repository
public interface TransporteRepository extends JpaRepository<Transporte, Long> {

    Optional<Transporte> findByCodigo(String codigo);

    Optional<Transporte> findByMatricula(String matricula);

    List<Transporte> findByCategoria(TransporteCategoria categoria);
}
