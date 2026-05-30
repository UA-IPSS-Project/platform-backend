package pt.florinhas.requisicoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import pt.florinhas.requisicoes.domain.RequisicaoPrioridade;

class CriarRequisicaoMaterialRequestTest {

    @Test
    void constructorAndAccessors_DeveFuncionar() {

        CriarRequisicaoMaterialRequest.ItemMaterialRequest item =
                new CriarRequisicaoMaterialRequest.ItemMaterialRequest(
                        1L,
                        5);

        CriarRequisicaoMaterialRequest request =
                new CriarRequisicaoMaterialRequest(
                        "Descricao",
                        RequisicaoPrioridade.MEDIA,
                        2L,
                        List.of(item),
                        null);

        assertEquals(
                "Descricao",
                request.descricao());

        assertEquals(
                RequisicaoPrioridade.MEDIA,
                request.prioridade());

        assertEquals(
                1,
                request.itens().size());

        assertEquals(
                1L,
                request.itens().get(0).materialId());

        assertEquals(
                5,
                request.itens().get(0).quantidade());
    }
}