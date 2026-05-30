package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

class CriarRequisicaoTransporteRequestTest {

    @Test
    void constructorAndAccessors_DeveFuncionar() {

        LocalDateTime saida =
                LocalDateTime.now();

        LocalDateTime regresso =
                saida.plusHours(2);

        CriarRequisicaoTransporteRequest request =
                new CriarRequisicaoTransporteRequest(
                        "Descricao",
                        RequisicaoPrioridade.URGENTE,
                        1L,
                        "Aveiro",
                        saida,
                        regresso,
                        5,
                        "Nuno",
                        List.of(1L, 2L),
                        1L,
                        null);

        assertEquals(
                "Descricao",
                request.descricao());

        assertEquals(
                RequisicaoPrioridade.URGENTE,
                request.prioridade());

        assertEquals(
                "Aveiro",
                request.destino());

        assertEquals(
                saida,
                request.dataHoraSaida());

        assertEquals(
                regresso,
                request.dataHoraRegresso());

        assertEquals(
                5,
                request.numeroPassageiros());

        assertEquals(
                "Nuno",
                request.condutor());

        assertEquals(
                2,
                request.transporteIds().size());

        assertEquals(
                1L,
                request.transporteId());
    }
}