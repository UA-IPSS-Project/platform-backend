package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoupaTest {

    @Test
    @DisplayName("deve criar roupa corretamente")
    void deveCriarRoupa() {
        ItemArmazem item = new ItemArmazem();
        item.setId(10L);

        Roupa roupa = new Roupa();

        roupa.setId(1L);
        roupa.setItem(item);
        roupa.setCategoria("Meias");
        roupa.setTamanho("39-42");
        roupa.setQuantidade(2);

        assertEquals(1L, roupa.getId());
        assertEquals(item, roupa.getItem());
        assertEquals("Meias", roupa.getCategoria());
        assertEquals("39-42", roupa.getTamanho());
        assertEquals(2, roupa.getQuantidade());
    }

    @Test
    @DisplayName("deve criar roupa com construtor all args")
    void deveCriarRoupaComAllArgs() {
        Roupa roupa = new Roupa();

        assertNotNull(roupa);
    }
}