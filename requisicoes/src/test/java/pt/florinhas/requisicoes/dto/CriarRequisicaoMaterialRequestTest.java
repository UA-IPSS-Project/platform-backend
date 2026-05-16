package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

class CriarRequisicaoMaterialRequestTest {

    @Test
    void record_DeveGuardarValores() {

        CriarRequisicaoMaterialRequest.ItemMaterialRequest item =
                new CriarRequisicaoMaterialRequest
                        .ItemMaterialRequest(
                                1L,
                                5);

        CriarRequisicaoMaterialRequest request =
                new CriarRequisicaoMaterialRequest(
                        "Descricao teste",
                        RequisicaoPrioridade.ALTA,
                        10L,
                        List.of(item),
                        null);

        assertEquals(
                "Descricao teste",
                request.descricao());

        assertEquals(
                RequisicaoPrioridade.ALTA,
                request.prioridade());

        assertEquals(
                10L,
                request.geridoPorId());

        assertEquals(
                1,
                request.itens().size());

        assertNotNull(
                request.itens().get(0));

        assertEquals(
                1L,
                request.itens().get(0).materialId());

        assertEquals(
                5,
                request.itens().get(0).quantidade());

        assertEquals(
                null,
                request.periodica());
    }

    @Test
    void itemMaterialRequest_DeveGuardarValores() {

        CriarRequisicaoMaterialRequest.ItemMaterialRequest item =
                new CriarRequisicaoMaterialRequest
                        .ItemMaterialRequest(
                                15L,
                                3);

        assertEquals(
                15L,
                item.materialId());

        assertEquals(
                3,
                item.quantidade());
    }
}