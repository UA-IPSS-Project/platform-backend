package pt.florinhas.marcacoes.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AtualizarConsumosRequestTest {

    @Test
    void deveDefinirValores() {

        AtualizarConsumosRequest request =
                new AtualizarConsumosRequest();

        request.setQuantidadeProdutos(5);
        request.setQuantidadeRoupa(2);
        request.setObservacoesConsumo("Teste");
        request.setFuncionarioId(1L);

        assertEquals(5, request.getQuantidadeProdutos());
        assertEquals(2, request.getQuantidadeRoupa());
        assertEquals("Teste", request.getObservacoesConsumo());
        assertEquals(1L, request.getFuncionarioId());
    }
}