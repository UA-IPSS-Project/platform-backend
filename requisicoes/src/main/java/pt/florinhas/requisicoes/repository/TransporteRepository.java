package pt.florinhas.requisicoes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.florinhas.requisicoes.domain.Transporte;
import pt.florinhas.requisicoes.domain.TransporteCategoria;

@Repository
public interface TransporteRepository extends JpaRepository<Transporte, Long> {

    Optional<Transporte> findByCodigo(String codigo);

    Optional<Transporte> findByMatricula(String matricula);

    List<Transporte> findByCategoria(TransporteCategoria categoria);

    @Modifying
    @Query("UPDATE Transporte t SET t.categoria = :destino WHERE t.categoria = :origem")
    int updateCategoriaByCategoria(
            @Param("origem") TransporteCategoria origem,
            @Param("destino") TransporteCategoria destino);
}
