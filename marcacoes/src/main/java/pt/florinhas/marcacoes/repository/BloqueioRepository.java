package pt.florinhas.marcacoes.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pt.florinhas.marcacoes.domain.BloqueioAgenda;

/**
 * Repositório Spring Data JPA para a entidade {@link BloqueioAgenda}.
 *
 * Responsabilidades:
 * - Consultas CRUD standard (herdadas de JpaRepository).
 * - Consultas derivadas por convenção (ex.: findByData).
 * - Consulta custom para detetar sobreposição de intervalos num dado dia.
 */
public interface BloqueioRepository extends JpaRepository<BloqueioAgenda, Long> {

       /**
        * Devolve todos os bloqueios registados para uma data específica.
        *
        * Método derivado pelo nome (Spring Data): traduz-se num SELECT por b.data =
        * :data.
        *
        * param data dia civil a consultar
        * return lista de bloqueios nesse dia
        */
       // Buscar todos os bloqueios de um dia específico
       List<BloqueioAgenda> findByData(LocalDate data);

       /**
        * Busca bloqueios num intervalo de datas (inclusive).
        * Usado para evitar carregar a tabela inteira em memória.
        */
       List<BloqueioAgenda> findByDataBetween(LocalDate start, LocalDate end);

       /**
        * Verifica se existe sobreposição entre um novo intervalo [inicio, fim]
        * e bloqueios já registados para a mesma data.
        *
        * Lógica de overlap (intervalos abertos): (InicioA < FimB) AND (FimA > InicioB)
        * - Se o novo início for anterior ao fim existente
        * - E o novo fim for posterior ao início existente
        * => há interseção.
        *
        * param data data a considerar
        * param inicio hora de início do novo bloqueio
        * param fim hora de fim do novo bloqueio
        * return true se existir pelo menos um bloqueio que colide, false caso
        * contrário
        */
       // Verificar se já existe algum bloqueio que colida com o horário que queremos
       // inserir
       // Lógica de Overlap: (InicioA < FimB) e (FimA > InicioB)
       @Query("SELECT COUNT(b) > 0 FROM BloqueioAgenda b " +
                     "WHERE b.data = :data " +
                     "AND b.horaInicio < :fim " +
                     "AND b.horaFim > :inicio")
       boolean existeSobreposicao(@Param("data") LocalDate data,
                     @Param("inicio") LocalTime inicio,
                     @Param("fim") LocalTime fim);
}
