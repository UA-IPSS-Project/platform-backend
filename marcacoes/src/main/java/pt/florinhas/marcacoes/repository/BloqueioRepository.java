package pt.florinhas.marcacoes.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pt.florinhas.marcacoes.domain.BloqueioAgenda;

public interface BloqueioRepository extends JpaRepository<BloqueioAgenda, Long> {

    // Buscar todos os bloqueios de um dia específico
    List<BloqueioAgenda> findByData(LocalDate data);

    // Verificar se já existe algum bloqueio que colida com o horário que queremos inserir
    // Lógica de Overlap: (InicioA < FimB) e (FimA > InicioB)
    @Query("SELECT COUNT(b) > 0 FROM BloqueioAgenda b " +
           "WHERE b.data = :data " +
           "AND b.horaInicio < :fim " +
           "AND b.horaFim > :inicio")
    boolean existeSobreposicao(@Param("data") LocalDate data, 
                               @Param("inicio") LocalTime inicio, 
                               @Param("fim") LocalTime fim);
}