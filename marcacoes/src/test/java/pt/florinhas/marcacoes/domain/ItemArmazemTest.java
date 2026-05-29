package pt.florinhas.marcacoes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ItemArmazemTest {

    @Test
    void itemArmazem_DeveGuardarValores() {

        ItemArmazem item = new ItemArmazem();

        item.setId(1L);
        item.setCategoria("HIGIENE");
        item.setNome("Champô");
        item.setQuantidade(10);
        item.setQuantidadeMinima(2);
        item.setUnidade("un");
        item.setMarca("Marca");
        item.setTamanho("L");
        item.setVolume(1.5);
        item.setDescricao("Teste");

        assertEquals(1L, item.getId());
        assertEquals("HIGIENE", item.getCategoria());
        assertEquals("Champô", item.getNome());
        assertEquals(10, item.getQuantidade());
        assertEquals(2, item.getQuantidadeMinima());
        assertEquals("un", item.getUnidade());
        assertEquals("Marca", item.getMarca());
        assertEquals("L", item.getTamanho());
        assertEquals(1.5, item.getVolume());
        assertEquals("Teste", item.getDescricao());
    }
}