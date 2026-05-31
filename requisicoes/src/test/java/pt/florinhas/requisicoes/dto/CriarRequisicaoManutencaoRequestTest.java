package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

class CriarRequisicaoManutencaoRequestTest {

    @Test
    void constructorAndAccessors_DeveFuncionar() {

        ManutencaoItemRequest item =
                new ManutencaoItemRequest(
                        1L,
                        null,
                        "Obs");

        CriarRequisicaoManutencaoRequest request =
                new CriarRequisicaoManutencaoRequest(
                        "Descricao",
                        RequisicaoPrioridade.ALTA,
                        2L,
                        List.of(item),
                        null);

        assertEquals(
                "Descricao",
                request.descricao());

        assertEquals(
                RequisicaoPrioridade.ALTA,
                request.prioridade());

        assertEquals(
                2L,
                request.geridoPorId());

        assertEquals(
                1,
                request.manutencaoItens().size());
    }
}