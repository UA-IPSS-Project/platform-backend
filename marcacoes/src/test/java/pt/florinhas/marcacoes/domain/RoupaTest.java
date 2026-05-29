package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RoupaTest {

    @Test
    void roupa_DeveGuardarValores() {

        Roupa roupa = new Roupa();

        roupa.setId(1L);
        roupa.setCategoria("CALÇAS");
        roupa.setTamanho("L");
        roupa.setQuantidade(2);

        assertEquals(1L, roupa.getId());
        assertEquals("CALÇAS", roupa.getCategoria());
        assertEquals("L", roupa.getTamanho());
        assertEquals(2, roupa.getQuantidade());
    }
}