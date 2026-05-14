package pt.florinhas.requisicoes.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ManutencaoItemTest {

    @Test
    void gettersAndSetters_DeveFuncionar() {

        ManutencaoItem item =
                new ManutencaoItem();

        item.setId(1L);
        item.setCategoria("CATL");
        item.setEspaco("Sala A");
        item.setItemVerificacao("Janela");

        assertEquals(1L, item.getId());
        assertEquals("CATL", item.getCategoria());
        assertEquals("Sala A", item.getEspaco());
        assertEquals("Janela", item.getItemVerificacao());
    }
}