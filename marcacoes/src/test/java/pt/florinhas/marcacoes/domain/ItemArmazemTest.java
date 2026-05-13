package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemArmazemTest {

    @Test
    @DisplayName("deve criar ItemArmazem com getters e setters")
    void deveCriarItemArmazem() {
        ItemArmazem item = new ItemArmazem();

        item.setId(1L);
        item.setCategoria("HIGIENE");
        item.setNome("Champô");
        item.setQuantidade(10);
        item.setQuantidadeMinima(5);
        item.setUnidade("un");
        item.setMarca("Marca X");
        item.setTamanho("M");
        item.setVolume(1.5);
        item.setDescricao("Descrição teste");

        assertEquals(1L, item.getId());
        assertEquals("HIGIENE", item.getCategoria());
        assertEquals("Champô", item.getNome());
        assertEquals(10, item.getQuantidade());
        assertEquals(5, item.getQuantidadeMinima());
        assertEquals("un", item.getUnidade());
        assertEquals("Marca X", item.getMarca());
        assertEquals("M", item.getTamanho());
        assertEquals(1.5, item.getVolume());
        assertEquals("Descrição teste", item.getDescricao());
    }

    @Test
    @DisplayName("deve criar ItemArmazem usando construtor all args")
    void deveCriarComAllArgsConstructor() {
        ItemArmazem item = new ItemArmazem(
                1L,
                "HIGIENE",
                "Champô",
                10,
                5,
                "un",
                "Marca",
                "M",
                1.0,
                "Descrição"
        );

        assertNotNull(item);
        assertEquals("Champô", item.getNome());
    }
}